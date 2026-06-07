import { apiRequest } from './apiClient.js';

export function login(request) {
  return apiRequest('/auth/login', {
    method: 'POST',
    body: request,
  });
}

export function register(request) {
  return apiRequest('/auth/register', {
    method: 'POST',
    body: request,
  });
}
