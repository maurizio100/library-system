const API_BASE = 'http://localhost:8080/api/catalog'
const OPEN_LIBRARY_SEARCH = 'https://openlibrary.org/search.json'

export interface BookLookupResult {
  title: string
  authors: string[]
  publicationYear: number | null
}

export function isValidIsbn(isbn: string): boolean {
  return /^\d{13}$/.test(isbn)
}

export async function lookupIsbn(isbn: string): Promise<BookLookupResult | null> {
  const response = await fetch(`${OPEN_LIBRARY_SEARCH}?isbn=${isbn}&fields=title,author_name,first_publish_year&limit=1`)
  if (!response.ok) {
    throw new Error('ISBN lookup service is currently unavailable — please try again later')
  }
  const data = await response.json()
  if (!data.docs || data.docs.length === 0) {
    return null
  }
  const doc = data.docs[0]
  return {
    title: doc.title ?? '',
    authors: doc.author_name ?? [],
    publicationYear: doc.first_publish_year ?? null,
  }
}

export async function checkIsbnExists(isbn: string): Promise<boolean> {
  const response = await fetch(`${API_BASE}/books?q=${encodeURIComponent(isbn)}`)
  if (!response.ok) {
    return false
  }
  const books: { isbn: string }[] = await response.json()
  return books.some((book) => book.isbn === isbn)
}
