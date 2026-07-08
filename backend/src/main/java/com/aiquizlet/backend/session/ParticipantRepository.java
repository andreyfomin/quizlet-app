package com.aiquizlet.backend.session;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    Optional<Participant> findBySession_IdAndSlackUserId(Long sessionId, String slackUserId);
}
