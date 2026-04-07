import { useState, useEffect } from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import Sidebar from './Sidebar'
import BrowseBooksPage from './BrowseBooksPage'
import AddBookPage from './AddBookPage'
import BookDetailsPage from './BookDetailsPage'
import BorrowBookPage from './BorrowBookPage'
import RegisterMemberPage from './RegisterMemberPage'
import NotFoundPage from './NotFoundPage'

type Theme = 'library' | 'videogame'

function App() {
  const [theme, setTheme] = useState<Theme>(
    () => (localStorage.getItem('theme') as Theme | null) ?? 'library'
  )

  useEffect(() => {
    if (theme === 'videogame') {
      document.documentElement.setAttribute('data-theme', 'videogame')
    } else {
      document.documentElement.removeAttribute('data-theme')
    }
    localStorage.setItem('theme', theme)
  }, [theme])

  const toggleTheme = () => setTheme((t) => (t === 'library' ? 'videogame' : 'library'))

  const isGameTheme = theme === 'videogame'

  return (
    <div className="flex min-h-screen font-sans">
      <Sidebar theme={theme} onToggleTheme={toggleTheme} />
      <div className="flex-1 overflow-auto">
        <div className="max-w-[860px] mx-auto py-10 px-8">
          <header className="mb-8 border-b-2 border-border pb-6">
            <h1 className="font-heading text-5xl font-semibold tracking-wide text-text-heading m-0 mb-1 max-lg:text-3xl max-lg:my-5">
              {isGameTheme ? '>> LIBRARY.EXE' : 'The Great Library of Minas Tirith'}
            </h1>
            <p className="text-text text-lg italic m-0">
              {isGameTheme ? 'PLAYER 1 SELECT TOME — INSERT COIN' : 'A chronicle of all volumes known to the Realm of Gondor'}
            </p>
          </header>
          <Routes>
            <Route path="/" element={<Navigate to="/catalog/browse" replace />} />
            <Route path="/catalog/browse" element={<BrowseBooksPage />} />
            <Route path="/catalog/books/:isbn" element={<BookDetailsPage />} />
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
