import { NavLink } from 'react-router-dom'

const catalogLinks = [
  { to: '/catalog/browse', label: 'Browse Books' },
  { to: '/catalog/add', label: 'Add Book' },
]

const lendingLinks = [
  { to: '/lending/borrow', label: 'Borrow a Book' },
  { to: '/lending/register-member', label: 'Register Member' },
]

interface SidebarProps {
  theme: 'library' | 'videogame'
  onToggleTheme: () => void
}

function Sidebar({ theme, onToggleTheme }: SidebarProps) {
  const linkClass = ({ isActive }: { isActive: boolean }) =>
    `block py-2 px-3 rounded text-sm font-semibold font-heading tracking-wide transition-colors ${
      isActive
        ? 'bg-accent text-bg'
        : 'text-text-heading hover:bg-accent-bg'
    }`

  return (
    <nav
      className="w-56 shrink-0 border-r border-border py-6 px-4 flex flex-col gap-6"
      aria-label="Sidebar"
    >
      <div>
        <h2 className="text-xs font-bold font-heading uppercase tracking-widest text-text mb-2 px-3">
          Catalog
        </h2>
        <ul className="list-none m-0 p-0 flex flex-col gap-1">
          {catalogLinks.map((link) => (
            <li key={link.to}>
              <NavLink to={link.to} className={linkClass}>
                {link.label}
              </NavLink>
            </li>
          ))}
        </ul>
      </div>
      <div>
        <h2 className="text-xs font-bold font-heading uppercase tracking-widest text-text mb-2 px-3">
          Lending
        </h2>
        <ul className="list-none m-0 p-0 flex flex-col gap-1">
          {lendingLinks.map((link) => (
            <li key={link.to}>
              <NavLink to={link.to} className={linkClass}>
                {link.label}
              </NavLink>
            </li>
          ))}
        </ul>
      </div>
      <div className="mt-auto pt-4 border-t border-border">
        <button
          onClick={onToggleTheme}
          className="w-full py-2 px-3 text-xs font-semibold font-heading tracking-wide rounded border border-border text-text-heading hover:bg-accent-bg transition-colors text-left"
        >
          {theme === 'library' ? 'Switch to Game Mode' : 'Switch to Library Mode'}
        </button>
      </div>
    </nav>
  )
}

export default Sidebar
