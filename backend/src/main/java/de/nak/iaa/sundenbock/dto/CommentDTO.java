package de.nak.iaa.sundenbock.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CommentDTO(
        Long id,
        Long ticketId,
        Long parentCommentId,
        String author,
        LocalDateTime createdOn,
        String commentText,
        int likes,
        int dislikes,
        List<CommentDTO> replies
        ){}
