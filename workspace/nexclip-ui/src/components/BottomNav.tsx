import { useLocation, useNavigate } from 'react-router-dom';

const navItems = [
  { idx: '01', label: 'Studio', sub: 'projects', path: '/' },
  { idx: '02', label: 'Edit', sub: 'timeline', path: '/editor' },
  { idx: '03', label: 'Config', sub: 'settings', path: '/settings' },
];

export default function BottomNav() {
  const location = useLocation();
  const navigate = useNavigate();

  return (
    <div className="absolute bottom-0 left-0 right-0 grid grid-cols-3 bg-paper border-t-1.5 border-ink z-50">
      {navItems.map((item) => {
        const isActive = location.pathname === item.path || (item.path === '/editor' && location.pathname.startsWith('/editor'));
        return (
          <button
            key={item.path}
            onClick={() => navigate(item.path)}
            className={`relative py-3.5 px-2 pb-6 border-r-1.5 border-ink last:border-r-0 cursor-pointer ${
              isActive ? 'bg-ink text-paper' : 'text-ink'
            }`}
          >
            <span className="absolute top-1.5 left-2 font-mono text-[9px] font-bold tracking-wider">
              {item.idx}
            </span>
            <span className="block text-[11px] font-bold tracking-[2px] uppercase mt-3.5">
              {item.label}
            </span>
            <span className="block font-mono text-[8px] tracking-wider mt-0.5 opacity-60">
              {item.sub}
            </span>
          </button>
        );
      })}
    </div>
  );
}
