import { apiRequest } from './apiClient.js';

export function startSession(quizId) {
  return apiRequest(`/quizzes/${quizId}/sessions`, {
    method: 'POST',
  });
}

export function getSessionState(roomCode) {
  return apiRequest(`/sessions/${roomCode}/state`);
}

export function joinSession(request) {
  return apiRequest('/sessions/join', {
    method: 'POST',
    body: request,
  });
}

export function showNextQuestion(sessionId) {
  return apiRequest(`/sessions/${sessionId}/questions/next`, {
    method: 'POST',
  });
}

export function closeCurrentQuestion(sessionId) {
  return apiRequest(`/sessions/${sessionId}/questions/current/close`, {
    method: 'POST',
  });
}

export function finishSession(sessionId) {
  return apiRequest(`/sessions/${sessionId}/finish`, {
    method: 'POST',
  });
}

export function submitAnswer(sessionId, request) {
  return apiRequest(`/sessions/${sessionId}/answers`, {
    method: 'POST',
    body: request,
  });
}
