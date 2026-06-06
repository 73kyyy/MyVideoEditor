import { ReactNode } from 'react';

interface GlassPanelProps {
  children: ReactNode;
  className?: string;
  neon?: boolean;
  violet?: boolean;
}

export default function GlassPanel({ children, className = '', neon = false, violet = false }: GlassPanelProps) {
  return (
    <div
      className={`glass-light rounded-xl ${
        neon ? 'neon-border neon-glow' : violet ? 'violet-border' : 'border border-dim/30'
      } ${className}`}
    >
      {children}
    </div>
  );
}
