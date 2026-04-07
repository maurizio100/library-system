const API_BASE = 'http://localhost:8080/api'

export async function apiFetch<T>(path: string, init?: RequestInit): Promise<T> {
  const headers: HeadersInit = init?.body ? { 'Content-Type': 'application/json' } : {}
  const response = await fetch(`${API_BASE}${path}`, {
    ...init,
    headers: { ...headers, ...init?.headers },
  })
  if (!response.ok) {
    const data = await response.json().catch(() => ({}))
    throw new Error((data as { error?: string }).error || 'Request failed')
  }
  return response.json() as Promise<T>
}

export async function apiFetchVoid(path: string, init?: RequestInit): Promise<void> {
  const headers: HeadersInit = init?.body ? { 'Content-Type': 'application/json' } : {}
  const response = await fetch(`${API_BASE}${path}`, {
    ...init,
    headers: { ...headers, ...init?.headers },
  })
  if (!response.ok) {
    const data = await response.json().catch(() => ({}))
    throw new Error((data as { error?: string }).error || 'Request failed')
  }
}

export async function apiFetchNullable<T>(path: string): Promise<T | null> {
  const response = await fetch(`${API_BASE}${path}`)
  if (response.status === 404) return null
  if (!response.ok) {
    const data = await response.json().catch(() => ({}))
    throw new Error((data as { error?: string }).error || 'Request failed')
  }
  return response.json() as Promise<T>
}
