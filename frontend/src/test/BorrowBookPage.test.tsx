import { describe, it, expect, beforeAll, afterEach, afterAll } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { setupServer } from 'msw/node'
import { MemoryRouter } from 'react-router-dom'
import BorrowBookPage from '../BorrowBookPage'

const server = setupServer()

beforeAll(() => server.listen())
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

function createLoanHandler(
  status = 201,
  body: Record<string, unknown> = {}
) {
  return http.post('http://localhost:8080/api/lending/loans', () => {
    return HttpResponse.json(body, { status })
  })
}

function renderWithRouter(ui: React.ReactElement) {
  return render(<MemoryRouter>{ui}</MemoryRouter>)
}

describe('BorrowBookPage', () => {
  it('successfully borrows a book through the form', async () => {
    const user = userEvent.setup()

    server.use(
      createLoanHandler(201, {
        loanId: 'loan-001',
        memberId: 'm-001',
        copyBarcode: 'EJ-001',
        loanDate: '2026-03-31',
        dueDate: '2026-04-14',
        status: 'Active',
      })
    )

    renderWithRouter(<BorrowBookPage />)

    await user.type(screen.getByLabelText('Member ID'), 'm-001')
    await user.type(screen.getByLabelText('Copy Barcode'), 'EJ-001')
    await user.click(screen.getByRole('button', { name: 'Confirm Loan' }))

    await waitFor(() => {
      expect(screen.getByText(/The tome has been entrusted/)).toBeInTheDocument()
    })
    expect(screen.getByText('Member ID: m-001')).toBeInTheDocument()
    expect(screen.getByText('Copy Barcode: EJ-001')).toBeInTheDocument()
    expect(screen.getByText('Due Date: 2026-04-14')).toBeInTheDocument()
  })

  it('shows loan summary before confirmation', async () => {
    const user = userEvent.setup()

    renderWithRouter(<BorrowBookPage />)

    await user.type(screen.getByLabelText('Member ID'), 'm-001')
    await user.type(screen.getByLabelText('Copy Barcode'), 'EJ-001')

    expect(screen.getByText('Member ID: m-001')).toBeInTheDocument()
    expect(screen.getByText('Copy Barcode: EJ-001')).toBeInTheDocument()
  })

  it('displays error when member has reached borrowing limit', async () => {
    const user = userEvent.setup()

    server.use(
      createLoanHandler(409, { error: 'Borrowing limit reached' })
    )

    renderWithRouter(<BorrowBookPage />)

    await user.type(screen.getByLabelText('Member ID'), 'm-001')
    await user.type(screen.getByLabelText('Copy Barcode'), 'EJ-001')
    await user.click(screen.getByRole('button', { name: 'Confirm Loan' }))

    await waitFor(() => {
      expect(screen.getByText('Borrowing limit reached')).toBeInTheDocument()
    })
    expect(screen.queryByText(/The tome has been entrusted/)).not.toBeInTheDocument()
  })

  it('displays error when copy is not available', async () => {
    const user = userEvent.setup()

    server.use(
      createLoanHandler(409, { error: 'Copy is not available' })
    )

    renderWithRouter(<BorrowBookPage />)

    await user.type(screen.getByLabelText('Member ID'), 'm-001')
    await user.type(screen.getByLabelText('Copy Barcode'), 'EJ-001')
    await user.click(screen.getByRole('button', { name: 'Confirm Loan' }))

    await waitFor(() => {
      expect(screen.getByText('Copy is not available')).toBeInTheDocument()
    })
  })

  it('displays error for unknown member', async () => {
    const user = userEvent.setup()

    server.use(
      createLoanHandler(404, { error: 'Member not found' })
    )

    renderWithRouter(<BorrowBookPage />)

    await user.type(screen.getByLabelText('Member ID'), 'm-999')
    await user.type(screen.getByLabelText('Copy Barcode'), 'EJ-001')
    await user.click(screen.getByRole('button', { name: 'Confirm Loan' }))

    await waitFor(() => {
      expect(screen.getByText('Member not found')).toBeInTheDocument()
    })
  })

  it('disables confirm button when fields are empty', () => {
    renderWithRouter(<BorrowBookPage />)

    const confirmButton = screen.getByRole('button', { name: 'Confirm Loan' })
    expect(confirmButton).toBeDisabled()
  })
})
