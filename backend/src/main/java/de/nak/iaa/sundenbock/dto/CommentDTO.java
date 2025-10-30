package de.nak.iaa.sundenbock.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.List;

public record CommentDTO(
        Long id,
        Long ticketId,
        Long parentCommentId,
        UserDTO author,
        LocalDateTime createdOn,
        String commentText,
        int likes,
        int dislikes,
        @JsonIgnoreProperties("replies")
        List<CommentDTO> replies
        ){}
