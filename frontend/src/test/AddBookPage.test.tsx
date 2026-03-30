import { describe, it, expect, beforeAll, afterEach, afterAll } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { setupServer } from 'msw/node'
import AddBookPage from '../AddBookPage'

const server = setupServer()

beforeAll(() => server.listen())
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

function catalogBooksHandler(books: { isbn: string; title: string; authors: string[] }[] = []) {
  return http.get('http://localhost:8080/api/catalog/books', () => {
    return HttpResponse.json(books)
  })
}

function openLibraryHandler(docs: Record<string, unknown>[] = []) {
  return http.get('https://openlibrary.org/search.json', () => {
    return HttpResponse.json({ docs })
  })
}

function addBookHandler(status = 201, body: Record<string, unknown> = {}) {
  return http.post('http://localhost:8080/api/catalog/books', () => {
    return HttpResponse.json(body, { status })
  })
}

const noop = () => {}

describe('AddBookPage', () => {
  it('successfully looks up and adds a book by ISBN', async () => {
    const user = userEvent.setup()

    server.use(
      catalogBooksHandler([]),
      openLibraryHandler([
        { title: 'Effective Java', author_name: ['Joshua Bloch'], first_publish_year: 2018 },
      ]),
      addBookHandler(201, {
        isbn: '9780134685991',
        title: 'Effective Java',
        authors: ['Joshua Bloch'],
        publicationYear: 2018,
      })
    )

    render(<AddBookPage onBack={noop} />)

    const isbnInput = screen.getByLabelText('ISBN')
    await user.type(isbnInput, '9780134685991')
    await user.click(screen.getByRole('button', { name: 'Look Up' }))

    await waitFor(() => {
      expect(screen.getByDisplayValue('Effective Java')).toBeInTheDocument()
    })
    expect(screen.getByDisplayValue('Joshua Bloch')).toBeInTheDocument()
    expect(screen.getByDisplayValue('2018')).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Add to Catalog' }))

    await waitFor(() => {
      expect(screen.getByText('Book added to catalog')).toBeInTheDocument()
    })
  })

  it('allows editing resolved data before adding', async () => {
    const user = userEvent.setup()

    let capturedBody: Record<string, unknown> | null = null
    server.use(
      catalogBooksHandler([]),
      openLibraryHandler([
        {
          title: 'Head First Design Patterns',
          author_name: ['Eric Freeman'],
          first_publish_year: 2004,
        },
      ]),
      http.post('http://localhost:8080/api/catalog/books', async ({ request }) => {
        capturedBody = (await request.json()) as Record<string, unknown>
        return HttpResponse.json(
          { isbn: '9780596007126', title: capturedBody.title, authors: ['Eric Freeman'], publicationYear: 2004 },
          { status: 201 }
        )
      })
    )

    render(<AddBookPage onBack={noop} />)

    await user.type(screen.getByLabelText('ISBN'), '9780596007126')
    await user.click(screen.getByRole('button', { name: 'Look Up' }))

    await waitFor(() => {
      expect(screen.getByDisplayValue('Head First Design Patterns')).toBeInTheDocument()
    })

    const titleInput = screen.getByLabelText('Title')
    await user.clear(titleInput)
    await user.type(titleInput, 'Head First Design Patterns (Revised)')

    await user.click(screen.getByRole('button', { name: 'Add to Catalog' }))

    await waitFor(() => {
      expect(screen.getByText('Book added to catalog')).toBeInTheDocument()
    })
    expect(capturedBody).not.toBeNull()
    expect(capturedBody!.title).toBe('Head First Design Patterns (Revised)')
  })

  it('shows error when ISBN not found in lookup service', async () => {
    const user = userEvent.setup()

    server.use(catalogBooksHandler([]), openLibraryHandler([]))

    render(<AddBookPage onBack={noop} />)

    await user.type(screen.getByLabelText('ISBN'), '9780000000000')
    await user.click(screen.getByRole('button', { name: 'Look Up' }))

    await waitFor(() => {
      expect(screen.getByText('No book found for this ISBN')).toBeInTheDocument()
    })
    expect(screen.queryByRole('button', { name: 'Add to Catalog' })).not.toBeInTheDocument()
  })

  it('shows error when ISBN already exists in the catalog', async () => {
    const user = userEvent.setup()

    server.use(
      catalogBooksHandler([
        { isbn: '9780134685991', title: 'Effective Java', authors: ['Joshua Bloch'] },
      ])
    )

    render(<AddBookPage onBack={noop} />)

    await user.type(screen.getByLabelText('ISBN'), '9780134685991')
    await user.click(screen.getByRole('button', { name: 'Look Up' }))

    await waitFor(() => {
      expect(
        screen.getByText('A book with this ISBN already exists in the catalog')
      ).toBeInTheDocument()
    })
    expect(screen.queryByRole('button', { name: 'Add to Catalog' })).not.toBeInTheDocument()
  })

  it('shows error when lookup service is unavailable', async () => {
    const user = userEvent.setup()

    server.use(
      catalogBooksHandler([]),
      http.get('https://openlibrary.org/search.json', () => {
        return new HttpResponse(null, { status: 500 })
      })
    )

    render(<AddBookPage onBack={noop} />)

    await user.type(screen.getByLabelText('ISBN'), '9780134685991')
    await user.click(screen.getByRole('button', { name: 'Look Up' }))

    await waitFor(() => {
      expect(
        screen.getByText('ISBN lookup service is currently unavailable — please try again later')
      ).toBeInTheDocument()
    })
    expect(screen.queryByRole('button', { name: 'Add to Catalog' })).not.toBeInTheDocument()
  })

  it('rejects invalid ISBN format without making a request', async () => {
    const user = userEvent.setup()

    render(<AddBookPage onBack={noop} />)

    await user.type(screen.getByLabelText('ISBN'), '123')

    expect(screen.getByText('Invalid ISBN format')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Look Up' })).toBeDisabled()
  })

  it('handles incomplete data with missing publication year', async () => {
    const user = userEvent.setup()

    server.use(
      catalogBooksHandler([]),
      openLibraryHandler([{ title: 'Unknown Book', author_name: ['Some Author'] }]),
      addBookHandler(201, {
        isbn: '9781234567897',
        title: 'Unknown Book',
        authors: ['Some Author'],
        publicationYear: 2020,
      })
    )

    render(<AddBookPage onBack={noop} />)

    await user.type(screen.getByLabelText('ISBN'), '9781234567897')
    await user.click(screen.getByRole('button', { name: 'Look Up' }))

    await waitFor(() => {
      expect(screen.getByDisplayValue('Unknown Book')).toBeInTheDocument()
    })
    expect(screen.getByDisplayValue('Some Author')).toBeInTheDocument()

    const yearInput = screen.getByLabelText('Publication Year')
    expect(yearInput).toHaveValue('')
    expect(screen.getByText('Publication year is missing')).toBeInTheDocument()

    await user.type(yearInput, '2020')
    await user.click(screen.getByRole('button', { name: 'Add to Catalog' }))

    await waitFor(() => {
      expect(screen.getByText('Book added to catalog')).toBeInTheDocument()
    })
  })
})
