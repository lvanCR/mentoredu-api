package com.mentoredu.forum.repository;

import com.mentoredu.auth.entity.User;
import com.mentoredu.forum.model.Answer;
import com.mentoredu.forum.model.Comment;
import com.mentoredu.forum.model.Reaction;
import com.mentoredu.forum.model.Thread;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReactionRepository extends JpaRepository<Reaction, UUID> {

    Optional<Reaction> findByUserAndThread(User user, Thread thread);

    Optional<Reaction> findByUserAndAnswer(User user, Answer answer);

    Optional<Reaction> findByUserAndComment(User user, Comment comment);
}
