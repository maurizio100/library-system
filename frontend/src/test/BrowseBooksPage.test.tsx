import { describe, it, expect, beforeAll, afterEach, afterAll } from 'vitest'
import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { setupServer } from 'msw/node'
import { MemoryRouter } from 'react-router-dom'
import BrowseBooksPage from '../BrowseBooksPage'

const catalogBooks = [
  {
    isbn: '9780134685991',
    title: 'Effective Java',
    authors: ['Joshua Bloch'],
    publicationYear: 2018,
    totalCopies: 0,
    availableCopies: 0,
  },
  {
    isbn: '9780132350884',
    title: 'Clean Code',
    authors: ['Robert Martin'],
    publicationYear: 2008,
    totalCopies: 0,
    availableCopies: 0,
  },
]

function listBooksHandler(books = catalogBooks) {
  return http.get('http://localhost:8080/api/catalog/books', ({ request }) => {
    const url = new URL(request.url)
    const q = url.searchParams.get('q')
    if (q) {
      const filtered = books.filter(
        (b) =>
          b.title.toLowerCase().includes(q.toLowerCase()) ||
          b.isbn.includes(q)
      )
      return HttpResponse.json(filtered)
    }
    return HttpResponse.json(books)
  })
}

function registerCopyHandler(
  status = 201,
  body: Record<string, unknown> = {}
) {
  return http.post(
    'http://localhost:8080/api/catalog/books/:isbn/copies',
    () => {
      return HttpResponse.json(body, { status })
    }
  )
}

const server = setupServer()

beforeAll(() => server.listen())
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

function renderPage() {
  return render(
    <MemoryRouter>
      <BrowseBooksPage />
    </MemoryRouter>
  )
}

describe('Register a Copy via the UI', () => {
  it('registers a copy by selecting a book from the catalog list', async () => {
    const user = userEvent.setup()

    server.use(
      listBooksHandler(),
      registerCopyHandler(201, {
        barcode: 'EJ-001',
        isbn: '9780134685991',
        status: 'Available',
      })
    )

    renderPage()

    await waitFor(() => {
      expect(screen.getByText('Effective Java')).toBeInTheDocument()
    })

    const ejRow = screen.getByText('Effective Java').closest('[class*="py-5"]')!
    await user.click(within(ejRow as HTMLElement).getByRole('button', { name: 'Register Copy' }))

    const barcodeInput = screen.getByLabelText('Barcode')
    await user.type(barcodeInput, 'EJ-001')
    await user.click(screen.getByRole('button', { name: 'Confirm' }))

    await waitFor(() => {
      expect(
        screen.getByText('Copy "EJ-001" for "Effective Java" registered')
      ).toBeInTheDocument()
    })

    // Copy count should be updated to 1/1
    expect(within(ejRow as HTMLElement).getByText('1/1')).toBeInTheDocument()
  })

  it('registers a copy by selecting a book from search results', async () => {
    const user = userEvent.setup()

    server.use(
      listBooksHandler(),
      registerCopyHandler(201, {
        barcode: 'CC-001',
        isbn: '9780132350884',
        status: 'Available',
      })
    )

    renderPage()

    await waitFor(() => {
      expect(screen.getByText('Effective Java')).toBeInTheDocument()
    })

    const searchInput = screen.getByPlaceholderText(/Search the archives/)
    await user.type(searchInput, 'Clean Code')
    await user.click(screen.getByRole('button', { name: 'Seek' }))

    await waitFor(() => {
      expect(screen.getByText('Clean Code')).toBeInTheDocument()
    })

    const ccRow = screen.getByText('Clean Code').closest('[class*="py-5"]')!
    await user.click(within(ccRow as HTMLElement).getByRole('button', { name: 'Register Copy' }))

    const barcodeInput = screen.getByLabelText('Barcode')
    await user.type(barcodeInput, 'CC-001')
    await user.click(screen.getByRole('button', { name: 'Confirm' }))

    await waitFor(() => {
      expect(
        screen.getByText('Copy "CC-001" for "Clean Code" registered')
      ).toBeInTheDocument()
    })
  })

  it('registers multiple copies one at a time', async () => {
    const user = userEvent.setup()

    let copyCount = 0
    server.use(
      listBooksHandler(),
      http.post(
        'http://localhost:8080/api/catalog/books/:isbn/copies',
        async ({ request }) => {
          const body = (await request.json()) as { barcode: string }
          copyCount++
          return HttpResponse.json(
            {
              barcode: body.barcode,
              isbn: '9780134685991',
              status: 'Available',
            },
            { status: 201 }
          )
        }
      )
    )

    renderPage()

    await waitFor(() => {
      expect(screen.getByText('Effective Java')).toBeInTheDocument()
    })

    const ejRow = screen.getByText('Effective Java').closest('[class*="py-5"]')!

    // Register first copy
    await user.click(within(ejRow as HTMLElement).getByRole('button', { name: 'Register Copy' }))
    await user.type(screen.getByLabelText('Barcode'), 'EJ-001')
    await user.click(screen.getByRole('button', { name: 'Confirm' }))

    await waitFor(() => {
      expect(
        screen.getByText('Copy "EJ-001" for "Effective Java" registered')
      ).toBeInTheDocument()
    })
    expect(within(ejRow as HTMLElement).getByText('1/1')).toBeInTheDocument()

    // Register second copy — form should still be open
    await user.type(screen.getByLabelText('Barcode'), 'EJ-002')
    await user.click(screen.getByRole('button', { name: 'Confirm' }))

    await waitFor(() => {
      expect(
        screen.getByText('Copy "EJ-002" for "Effective Java" registered')
      ).toBeInTheDocument()
    })
    expect(within(ejRow as HTMLElement).getByText('2/2')).toBeInTheDocument()
    expect(copyCount).toBe(2)
  })

  it('displays error when barcode already exists', async () => {
    const user = userEvent.setup()

    server.use(
      listBooksHandler(),
      registerCopyHandler(409, { error: 'Barcode already exists' })
    )

    renderPage()

    await waitFor(() => {
      expect(screen.getByText('Effective Java')).toBeInTheDocument()
    })

    const ejRow = screen.getByText('Effective Java').closest('[class*="py-5"]')!
    await user.click(within(ejRow as HTMLElement).getByRole('button', { name: 'Register Copy' }))

    await user.type(screen.getByLabelText('Barcode'), 'EJ-001')
    await user.click(screen.getByRole('button', { name: 'Confirm' }))

    await waitFor(() => {
      expect(screen.getByText('Barcode already exists')).toBeInTheDocument()
    })

    // Copy count should remain 0/0
    expect(within(ejRow as HTMLElement).getByText('0/0')).toBeInTheDocument()
  })

  it('prevents submission with empty barcode', async () => {
    const user = userEvent.setup()

    server.use(listBooksHandler())

    renderPage()

    await waitFor(() => {
      expect(screen.getByText('Effective Java')).toBeInTheDocument()
    })

    const ejRow = screen.getByText('Effective Java').closest('[class*="py-5"]')!
    await user.click(within(ejRow as HTMLElement).getByRole('button', { name: 'Register Copy' }))

    const confirmButton = screen.getByRole('button', { name: 'Confirm' })
    expect(confirmButton).toBeDisabled()
  })
})
