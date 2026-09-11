const apiBaseUrl = import.meta.env.VITE_API_URL ?? ''
export const publicBaseUrl = import.meta.env.VITE_PUBLIC_BASE_URL ?? window.location.origin

export async function api(token, path, options = {}) {
  const headers = { ...(options.body instanceof FormData ? {} : { 'Content-Type': 'application/json' }), ...options.headers }
  if (token) headers.Authorization = `Bearer ${token}`
  const response = await fetch(`${apiBaseUrl}${path}`, { ...options, headers })
  if (!response.ok) {
    const body = await response.json().catch(() => ({}))
    throw new Error(body.message ?? '요청을 처리하지 못했습니다.')
  }
  return response.status === 204 ? null : response.json()
}
