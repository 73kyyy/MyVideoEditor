import { useLocation, useNavigate } from 'react-router-dom';
import { Zap, User } from 'lucide-react';

const navItems = [
  { path: '/', label: 'Workspace', short: 'WS' },
  { path: '/studio', label: 'Clip Studio', short: 'CS' },
  { path: '/export', label: 'Export Hub', short: 'EH' },
];

export default function TopNav() {
  const location = useLocation();
  const navigate = useNavigate();

  return (
    <nav className="glass sticky top-0 z-50 border-b border-dim/30">
      <div className="flex items-center justify-between px-6 h-14">
        {/* Logo */}
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-md bg-neon/10 border border-neon/30 flex items-center justify-center">
            <Zap className="w-4 h-4 text-neon" />
          </div>
          <div>
            <span className="font-grotesk font-bold text-base tracking-tight">DULA</span>
            <span className="font-mono text-[10px] text-neon ml-1.5 tracking-wider">AUTO CLIP</span>
          </div>
        </div>

        {/* Nav Tabs */}
        <div className="flex items-center gap-1">
          {navItems.map((item) => {
            const isActive = location.pathname === item.path;
            return (
              <button
                key={item.path}
                onClick={() => navigate(item.path)}
                className={`relative px-4 py-2 rounded-md font-grotesk text-sm font-medium transition-all duration-200 ${
                  isActive
                    ? 'bg-neon/10 text-neon neon-border'
                    : 'text-muted hover:text-paper hover:bg-surface2/50'
                }`}
              >
                <span className="font-mono text-[9px] mr-1.5 opacity-50">{item.short}</span>
                {item.label}
                {isActive && (
                  <span className="absolute bottom-0 left-2 right-2 h-[2px] bg-neon rounded-full" />
                )}
              </button>
            );
          })}
        </div>

        {/* User */}
        <div className="flex items-center gap-3">
          <div className="font-mono text-[10px] text-muted">
            PRO <span className="text-neon">ACTIVE</span>
          </div>
          <div className="w-8 h-8 rounded-full bg-violet/20 border border-violet/30 flex items-center justify-center">
            <User className="w-4 h-4 text-violet" />
          </div>
        </div>
      </div>
    </nav>
  );
}
