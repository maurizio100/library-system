import { apiFetch, apiFetchNullable, apiFetchVoid } from './client'
import { normaliseIsbn } from '../isbnLookup'

export interface BookSearchResult {
  isbn: string
  title: string
  authors: string[]
  publicationYear: number
  totalCopies: number
  availableCopies: number
}

export interface CopyDetail {
  barcode: string
  status: string
}

export interface BookDetails {
  isbn: string
  title: string
  authors: string[]
  publicationYear: number
  coverUrl?: string | null
  copies: CopyDetail[]
}

export interface TitleSearchCandidate {
  isbn: string
  title: string
  authors: string[]
  publicationYear: number | null
  coverUrl?: string | null
}

export interface RegisterCopyResponse {
  barcode: string
  status?: string
}

export function searchBooks(query?: string): Promise<BookSearchResult[]> {
  const path = query ? `/catalog/books?q=${encodeURIComponent(query)}` : '/catalog/books'
  return apiFetch(path)
}

export function getBook(isbn: string): Promise<BookDetails | null> {
  return apiFetchNullable(`/catalog/books/${isbn}`)
}

export function addBook(book: {
  isbn: string
  title: string
  authors: string[]
  publicationYear: number
  coverUrl?: string | null
}): Promise<void> {
  return apiFetch('/catalog/books', { method: 'POST', body: JSON.stringify(book) })
}

export function registerCopy(isbn: string, barcode: string): Promise<RegisterCopyResponse> {
  return apiFetch(`/catalog/books/${isbn}/copies`, {
    method: 'POST',
    body: JSON.stringify({ barcode }),
  })
}

export function removeCopy(barcode: string): Promise<void> {
  return apiFetchVoid(`/catalog/copies/${encodeURIComponent(barcode)}`, { method: 'DELETE' })
}

export function searchBooksByTitle(title: string): Promise<TitleSearchCandidate[]> {
  return apiFetch(`/catalog/books/lookup?title=${encodeURIComponent(title)}`)
}

export async function checkIsbnExists(isbn: string): Promise<boolean> {
  const normalised = normaliseIsbn(isbn)
  try {
    const books = await apiFetch<{ isbn: string }[]>(
      `/catalog/books?q=${encodeURIComponent(normalised)}`
    )
    return books.some((book) => book.isbn === normalised)
  } catch {
    return false
  }
}
