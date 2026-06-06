import { ReactNode } from 'react';

interface PhoneFrameProps {
  children: ReactNode;
}

export default function PhoneFrame({ children }: PhoneFrameProps) {
  return (
    <div className="flex items-center justify-center min-h-screen p-5"
      style={{
        backgroundImage: `
          repeating-linear-gradient(0deg, transparent, transparent 39px, rgba(10,10,10,0.04) 39px, rgba(10,10,10,0.04) 40px),
          repeating-linear-gradient(90deg, transparent, transparent 39px, rgba(10,10,10,0.04) 39px, rgba(10,10,10,0.04) 40px)
        `,
      }}
    >
      <div
        className="w-[393px] h-[852px] bg-paper relative overflow-hidden border-[3px] border-ink"
        style={{ boxShadow: '12px 12px 0 #0A0A0A' }}
      >
        {children}
      </div>
    </div>
  );
}
