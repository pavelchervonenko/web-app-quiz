import { apiRequest } from './apiClient.js';

export function getMyQuizzes() {
  return apiRequest('/quizzes/my');
}

export function getQuiz(quizId) {
  return apiRequest(`/quizzes/${quizId}`);
}

export function createQuiz(request) {
  return apiRequest('/quizzes', {
    method: 'POST',
    body: request,
  });
}

export function updateQuiz(quizId, request) {
  return apiRequest(`/quizzes/${quizId}`, {
    method: 'PUT',
    body: request,
  });
}

export function deleteQuiz(quizId) {
  return apiRequest(`/quizzes/${quizId}`, {
    method: 'DELETE',
  });
}

export function addQuestion(quizId, request) {
  return apiRequest(`/quizzes/${quizId}/questions`, {
    method: 'POST',
    body: request,
  });
}

export function updateQuestion(quizId, questionId, request) {
  return apiRequest(`/quizzes/${quizId}/questions/${questionId}`, {
    method: 'PUT',
    body: request,
  });
}

export function deleteQuestion(quizId, questionId) {
  return apiRequest(`/quizzes/${quizId}/questions/${questionId}`, {
    method: 'DELETE',
  });
}
