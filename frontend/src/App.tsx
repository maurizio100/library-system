import { Routes, Route, Navigate } from 'react-router-dom'
import Sidebar from './Sidebar'
import BrowseBooksPage from './BrowseBooksPage'
import AddBookPage from './AddBookPage'
import BorrowBookPage from './BorrowBookPage'
import RegisterMemberPage from './RegisterMemberPage'
import NotFoundPage from './NotFoundPage'

function App() {
  return (
    <div className="flex min-h-screen font-sans">
      <Sidebar />
      <div className="flex-1 overflow-auto">
        <div className="max-w-[860px] mx-auto py-10 px-8">
          <header className="mb-8 border-b-2 border-border pb-6">
            <h1 className="font-heading text-5xl font-semibold tracking-wide text-text-heading m-0 mb-1 max-lg:text-3xl max-lg:my-5">
              The Great Library of Minas Tirith
            </h1>
            <p className="text-text text-lg italic m-0">
              A chronicle of all volumes known to the Realm of Gondor
            </p>
          </header>
          <Routes>
            <Route path="/" element={<Navigate to="/catalog/browse" replace />} />
            <Route path="/catalog/browse" element={<BrowseBooksPage />} />
            <Route path="/catalog/add" element={<AddBookPage />} />
            <Route path="/lending/borrow" element={<BorrowBookPage />} />
            <Route path="/lending/register-member" element={<RegisterMemberPage />} />
            <Route path="*" element={<NotFoundPage />} />
          </Routes>
        </div>
      </div>
    </div>
  )
}

export default App
