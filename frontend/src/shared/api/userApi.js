import { apiRequest } from './apiClient.js';

export function getMe() {
  return apiRequest('/users/me');
}

export function updateMe(request) {
  return apiRequest('/users/me', {
    method: 'PATCH',
    body: request,
  });
}

export function getOrganizedSessions() {
  return apiRequest('/users/me/organized-sessions');
}

export function getParticipations() {
  return apiRequest('/users/me/participations');
}
