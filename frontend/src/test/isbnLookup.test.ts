import { describe, it, expect, beforeAll, afterEach, afterAll } from 'vitest'
import { http, HttpResponse } from 'msw'
import { setupServer } from 'msw/node'
import { isValidIsbn, lookupIsbn, checkIsbnExists } from '../isbnLookup'

const server = setupServer()

beforeAll(() => server.listen())
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

describe('isValidIsbn', () => {
  it('accepts a 13-digit string', () => {
    expect(isValidIsbn('9780134685991')).toBe(true)
  })

  it('rejects a string shorter than 13 digits', () => {
    expect(isValidIsbn('123')).toBe(false)
  })

  it('rejects a string with non-digit characters', () => {
    expect(isValidIsbn('978-0134685991')).toBe(false)
  })

  it('rejects an empty string', () => {
    expect(isValidIsbn('')).toBe(false)
  })
})

describe('lookupIsbn', () => {
  it('returns book data when found', async () => {
    server.use(
      http.get('https://openlibrary.org/search.json', () => {
        return HttpResponse.json({
          docs: [
            {
              title: 'Effective Java',
              author_name: ['Joshua Bloch'],
              first_publish_year: 2018,
            },
          ],
        })
      })
    )

    const result = await lookupIsbn('9780134685991')
    expect(result).toEqual({
      title: 'Effective Java',
      authors: ['Joshua Bloch'],
      publicationYear: 2018,
    })
  })

  it('returns null when no docs found', async () => {
    server.use(
      http.get('https://openlibrary.org/search.json', () => {
        return HttpResponse.json({ docs: [] })
      })
    )

    const result = await lookupIsbn('9780000000000')
    expect(result).toBeNull()
  })

  it('returns null publication year when missing', async () => {
    server.use(
      http.get('https://openlibrary.org/search.json', () => {
        return HttpResponse.json({
          docs: [{ title: 'Some Book', author_name: ['Author'] }],
        })
      })
    )

    const result = await lookupIsbn('9781234567897')
    expect(result).toEqual({
      title: 'Some Book',
      authors: ['Author'],
      publicationYear: null,
    })
  })

  it('throws when service is unavailable', async () => {
    server.use(
      http.get('https://openlibrary.org/search.json', () => {
        return new HttpResponse(null, { status: 500 })
      })
    )

    await expect(lookupIsbn('9780134685991')).rejects.toThrow(
      'ISBN lookup service is currently unavailable'
    )
  })
})

describe('checkIsbnExists', () => {
  it('returns true when ISBN exists in catalog', async () => {
    server.use(
      http.get('http://localhost:8080/api/catalog/books', () => {
        return HttpResponse.json([
          { isbn: '9780134685991', title: 'Effective Java', authors: ['Joshua Bloch'] },
        ])
      })
    )

    const exists = await checkIsbnExists('9780134685991')
    expect(exists).toBe(true)
  })

  it('returns false when ISBN not in catalog', async () => {
    server.use(
      http.get('http://localhost:8080/api/catalog/books', () => {
        return HttpResponse.json([])
      })
    )

    const exists = await checkIsbnExists('9780000000000')
    expect(exists).toBe(false)
  })

  it('returns false on API error', async () => {
    server.use(
      http.get('http://localhost:8080/api/catalog/books', () => {
        return new HttpResponse(null, { status: 500 })
      })
    )

    const exists = await checkIsbnExists('9780134685991')
    expect(exists).toBe(false)
  })
})
