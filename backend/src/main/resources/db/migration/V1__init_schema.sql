CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    role VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT chk_users_role CHECK (role IN ('PARTICIPANT', 'ORGANIZER', 'ADMIN'))
);

CREATE TABLE quizzes (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_quizzes_owner FOREIGN KEY (owner_id) REFERENCES users (id),
    CONSTRAINT chk_quizzes_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
);

CREATE TABLE questions (
    id UUID PRIMARY KEY,
    quiz_id UUID NOT NULL,
    text TEXT NOT NULL,
    image_url VARCHAR(255),
    type VARCHAR(32) NOT NULL,
    time_limit_seconds INTEGER NOT NULL,
    points_correct INTEGER NOT NULL,
    points_incorrect INTEGER NOT NULL,
    position INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_questions_quiz FOREIGN KEY (quiz_id) REFERENCES quizzes (id),
    CONSTRAINT uk_questions_quiz_position UNIQUE (quiz_id, position),
    CONSTRAINT chk_questions_type CHECK (type IN ('SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'TRUE_FALSE')),
    CONSTRAINT chk_questions_time_limit CHECK (time_limit_seconds > 0),
    CONSTRAINT chk_questions_points_correct CHECK (points_correct >= 0),
    CONSTRAINT chk_questions_points_incorrect CHECK (points_incorrect >= 0),
    CONSTRAINT chk_questions_position CHECK (position > 0)
);

CREATE TABLE answer_options (
    id UUID PRIMARY KEY,
    question_id UUID NOT NULL,
    text TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL,
    position INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_answer_options_question FOREIGN KEY (question_id) REFERENCES questions (id),
    CONSTRAINT uk_answer_options_question_position UNIQUE (question_id, position),
    CONSTRAINT chk_answer_options_position CHECK (position > 0)
);

CREATE TABLE quiz_sessions (
    id UUID PRIMARY KEY,
    quiz_id UUID NOT NULL,
    organizer_id UUID NOT NULL,
    room_code VARCHAR(12) NOT NULL,
    status VARCHAR(32) NOT NULL,
    current_question_id UUID,
    current_question_started_at TIMESTAMP WITH TIME ZONE,
    current_question_ends_at TIMESTAMP WITH TIME ZONE,
    started_at TIMESTAMP WITH TIME ZONE,
    finished_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_quiz_sessions_quiz FOREIGN KEY (quiz_id) REFERENCES quizzes (id),
    CONSTRAINT fk_quiz_sessions_organizer FOREIGN KEY (organizer_id) REFERENCES users (id),
    CONSTRAINT fk_quiz_sessions_current_question FOREIGN KEY (current_question_id) REFERENCES questions (id),
    CONSTRAINT uk_quiz_sessions_room_code UNIQUE (room_code),
    CONSTRAINT chk_quiz_sessions_status CHECK (
        status IN ('WAITING', 'QUESTION_ACTIVE', 'QUESTION_CLOSED', 'FINISHED', 'CANCELLED')
    )
);

CREATE TABLE participant_sessions (
    id UUID PRIMARY KEY,
    quiz_session_id UUID NOT NULL,
    user_id UUID,
    display_name VARCHAR(100) NOT NULL,
    score INTEGER NOT NULL,
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL,
    finished_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_participant_sessions_quiz_session FOREIGN KEY (quiz_session_id) REFERENCES quiz_sessions (id),
    CONSTRAINT fk_participant_sessions_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uk_participant_sessions_session_display_name UNIQUE (quiz_session_id, display_name),
    CONSTRAINT chk_participant_sessions_score CHECK (score >= 0)
);

CREATE TABLE participant_answers (
    id UUID PRIMARY KEY,
    quiz_session_id UUID NOT NULL,
    participant_session_id UUID NOT NULL,
    question_id UUID NOT NULL,
    is_correct BOOLEAN NOT NULL,
    points_awarded INTEGER NOT NULL,
    answered_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_participant_answers_quiz_session FOREIGN KEY (quiz_session_id) REFERENCES quiz_sessions (id),
    CONSTRAINT fk_participant_answers_participant_session
        FOREIGN KEY (participant_session_id) REFERENCES participant_sessions (id),
    CONSTRAINT fk_participant_answers_question FOREIGN KEY (question_id) REFERENCES questions (id),
    CONSTRAINT uk_participant_answers_participant_question UNIQUE (participant_session_id, question_id),
    CONSTRAINT chk_participant_answers_points_awarded CHECK (points_awarded >= 0)
);

CREATE TABLE participant_answer_options (
    id UUID PRIMARY KEY,
    participant_answer_id UUID NOT NULL,
    answer_option_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_participant_answer_options_answer
        FOREIGN KEY (participant_answer_id) REFERENCES participant_answers (id),
    CONSTRAINT fk_participant_answer_options_option FOREIGN KEY (answer_option_id) REFERENCES answer_options (id),
    CONSTRAINT uk_participant_answer_options_answer_option UNIQUE (participant_answer_id, answer_option_id)
);

CREATE INDEX idx_quizzes_owner_id ON quizzes (owner_id);
CREATE INDEX idx_questions_quiz_id ON questions (quiz_id);
CREATE INDEX idx_answer_options_question_id ON answer_options (question_id);
CREATE INDEX idx_quiz_sessions_quiz_id ON quiz_sessions (quiz_id);
CREATE INDEX idx_quiz_sessions_organizer_id ON quiz_sessions (organizer_id);
CREATE INDEX idx_participant_sessions_quiz_session_id ON participant_sessions (quiz_session_id);
CREATE INDEX idx_participant_sessions_user_id ON participant_sessions (user_id);
CREATE INDEX idx_participant_answers_quiz_session_id ON participant_answers (quiz_session_id);
CREATE INDEX idx_participant_answers_participant_session_id ON participant_answers (participant_session_id);
CREATE INDEX idx_participant_answers_question_id ON participant_answers (question_id);
CREATE INDEX idx_participant_answer_options_answer_id ON participant_answer_options (participant_answer_id);
CREATE INDEX idx_participant_answer_options_option_id ON participant_answer_options (answer_option_id);
