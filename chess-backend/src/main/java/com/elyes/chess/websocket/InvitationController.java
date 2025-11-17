package com.elyes.chess.websocket;

import com.elyes.chess.game.Game;
import com.elyes.chess.game.GameService;
import com.elyes.chess.user.User;
import com.elyes.chess.user.UserRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class InvitationController {

    private final UserRepository userRepository;
    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;

    public InvitationController(UserRepository userRepository,
                                GameService gameService,
                                SimpMessagingTemplate messagingTemplate) {
        this.userRepository = userRepository;
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
    }

    // 1) Send an invitation
    @MessageMapping("/invite")
    public void invite(@Payload InviteMessage message) {
        User from = userRepository.findById(message.getFromUserId())
                .orElse(null);
        User to = userRepository.findById(message.getToUserId())
                .orElse(null);

        if (from == null || to == null) {
            return; // invalid ids, ignore
        }

        InviteNotification notification = new InviteNotification(
                InviteNotification.Type.INVITE,
                from.getId(),
                from.getUsername(),
                to.getId(),
                null
        );

        // send only to the invited player (toUserId)
        messagingTemplate.convertAndSend(
                "/topic/invitations." + to.getId(),
                notification
        );
    }

    // 2) Respond to an invitation (accept / refuse)
    @MessageMapping("/invite-response")
    public void respond(@Payload InviteResponseMessage message) {
        User from = userRepository.findById(message.getFromUserId())
                .orElse(null); // the one responding
        User to = userRepository.findById(message.getToUserId())
                .orElse(null); // original inviter

        if (from == null || to == null) {
            return;
        }

        if (message.isAccepted()) {
            // create game: inviter = white, invited = black
            Game game = gameService.createGame(
                    message.getToUserId(),     // white = inviter
                    message.getFromUserId()    // black = invited
            );

            InviteNotification notifToInviter = new InviteNotification(
                    InviteNotification.Type.ACCEPT,
                    from.getId(),
                    from.getUsername(),
                    to.getId(),
                    game.getId()
            );

            InviteNotification notifToInvited = new InviteNotification(
                    InviteNotification.Type.ACCEPT,
                    from.getId(),
                    from.getUsername(),
                    to.getId(),
                    game.getId()
            );

            // Notify both players on their own channel
            messagingTemplate.convertAndSend(
                    "/topic/invitations." + to.getId(),
                    notifToInviter
            );
            messagingTemplate.convertAndSend(
                    "/topic/invitations." + from.getId(),
                    notifToInvited
            );
        } else {
            // refused
            InviteNotification notification = new InviteNotification(
                    InviteNotification.Type.REFUSE,
                    from.getId(),
                    from.getUsername(),
                    to.getId(),
                    null
            );

            // Only the inviter needs to know it was refused
            messagingTemplate.convertAndSend(
                    "/topic/invitations." + to.getId(),
                    notification
            );
        }
    }
}
