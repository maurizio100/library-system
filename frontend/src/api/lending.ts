import { apiFetch } from './client'

export interface Member {
  memberId: string
  name: string
}

export interface LoanResponse {
  loanId: string
  copyBarcode: string
  loanDate: string
  dueDate: string
}

export interface RegisterMemberResponse {
  memberId: string
}

export function getMembers(): Promise<Member[]> {
  return apiFetch('/lending/members')
}

export function createLoan(memberId: string, copyBarcode: string): Promise<LoanResponse> {
  return apiFetch('/lending/loans', {
    method: 'POST',
    body: JSON.stringify({ memberId, copyBarcode }),
  })
}

export function registerMember(name: string, email: string): Promise<RegisterMemberResponse> {
  return apiFetch('/lending/members', {
    method: 'POST',
    body: JSON.stringify({ name, email }),
  })
}
