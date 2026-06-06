import { useEffect, useRef } from 'react';

interface ScopeCanvasProps {
  type: 'waveform' | 'vector' | 'parade' | 'histogram';
}

export default function ScopeCanvas({ type }: ScopeCanvasProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const w = 160;
    const h = 60;
    ctx.clearRect(0, 0, w, h);

    if (type === 'waveform') {
      ctx.fillStyle = '#0A0A0A';
      ctx.fillRect(0, 0, w, h);
      for (let x = 0; x < w; x += 2) {
        const v = 20 + Math.random() * 30;
        const g = ctx.createLinearGradient(x, h - v, x, h);
        g.addColorStop(0, 'rgba(255,61,0,0.1)');
        g.addColorStop(0.5, 'rgba(255,61,0,0.6)');
        g.addColorStop(1, 'rgba(255,61,0,0.1)');
        ctx.fillStyle = g;
        ctx.fillRect(x, h - v, 2, v);
      }
    } else if (type === 'vector') {
      ctx.fillStyle = '#0A0A0A';
      ctx.fillRect(0, 0, w, h);
      ctx.strokeStyle = 'rgba(255,61,0,0.3)';
      ctx.beginPath();
      ctx.arc(w / 2, h / 2, 25, 0, Math.PI * 2);
      ctx.stroke();
      ctx.beginPath();
      ctx.arc(w / 2, h / 2, 15, 0, Math.PI * 2);
      ctx.stroke();
      for (let i = 0; i < 60; i++) {
        const a = Math.random() * Math.PI * 2;
        const r = Math.random() * 22;
        ctx.fillStyle = `rgba(255,61,0,${0.3 + Math.random() * 0.5})`;
        ctx.fillRect(w / 2 + Math.cos(a) * r, h / 2 + Math.sin(a) * r, 1.5, 1.5);
      }
    } else if (type === 'parade') {
      ctx.fillStyle = '#0A0A0A';
      ctx.fillRect(0, 0, w, h);
      const colors = ['rgba(255,60,60,', 'rgba(60,255,60,', 'rgba(60,100,255,'];
      colors.forEach((c, i) => {
        for (let x = i * 53; x < (i + 1) * 53; x += 2) {
          const v = 15 + Math.random() * 35;
          ctx.fillStyle = c + (0.2 + Math.random() * 0.4) + ')';
          ctx.fillRect(x, h - v, 2, v);
        }
      });
    } else {
      ctx.fillStyle = '#0A0A0A';
      ctx.fillRect(0, 0, w, h);
      for (let x = 0; x < w; x++) {
        const v = Math.sin(x / 25) * 15 + 25 + Math.random() * 10;
        ctx.fillStyle = `rgba(255,61,0,${0.15 + v / 80})`;
        ctx.fillRect(x, h - v, 1, v);
      }
    }
  }, [type]);

  return (
    <canvas
      ref={canvasRef}
      width={160}
      height={60}
      className="w-full block"
    />
  );
}
