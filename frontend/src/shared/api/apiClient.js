const API_BASE_URL = '/api';

export class ApiError extends Error {
  constructor(response, payload) {
    super(payload?.message || `Request failed with status ${response.status}`);
    this.name = 'ApiError';
    this.status = response.status;
    this.payload = payload;
    this.fieldErrors = payload?.fieldErrors || {};
  }
}

let accessTokenProvider = () => null;

export function setAccessTokenProvider(provider) {
  accessTokenProvider = provider;
}

export async function apiRequest(path, options = {}) {
  const headers = new Headers(options.headers || {});
  const token = accessTokenProvider();

  if (options.body !== undefined && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
    body: options.body === undefined ? undefined : JSON.stringify(options.body),
  });

  if (response.status === 204) {
    return null;
  }

  const payload = await parseJson(response);

  if (!response.ok) {
    throw new ApiError(response, payload);
  }

  return payload;
}

async function parseJson(response) {
  const text = await response.text();

  if (!text) {
    return null;
  }

  return JSON.parse(text);
}
