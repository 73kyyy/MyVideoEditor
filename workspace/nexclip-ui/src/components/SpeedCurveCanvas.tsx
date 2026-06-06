import { useEffect, useRef } from 'react';
import type { SpeedCurveType } from '@/types';

interface SpeedCurveCanvasProps {
  type: SpeedCurveType;
}

const curves: Record<SpeedCurveType, number[][]> = {
  linear: [[0, 120], [350, 0]],
  in: [[0, 120], [105, 120], [350, 0]],
  out: [[0, 120], [245, 0], [350, 0]],
  both: [[0, 120], [105, 60], [245, 60], [350, 0]],
  bounce: [[0, 120], [70, 72], [122, 24], [175, 48], [227, 12], [280, 18], [350, 0]],
  custom: [[0, 120], [70, 96], [175, 36], [280, 12], [350, 0]],
};

export default function SpeedCurveCanvas({ type }: SpeedCurveCanvasProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const w = 350;
    const h = 120;
    ctx.clearRect(0, 0, w, h);

    // Grid
    ctx.strokeStyle = '#0A0A0A';
    ctx.lineWidth = 0.5;
    for (let i = 0; i <= 4; i++) {
      ctx.beginPath();
      ctx.moveTo(0, (h * i) / 4);
      ctx.lineTo(w, (h * i) / 4);
      ctx.stroke();
    }
    for (let i = 0; i <= 6; i++) {
      ctx.beginPath();
      ctx.moveTo((w * i) / 6, 0);
      ctx.lineTo((w * i) / 6, h);
      ctx.stroke();
    }

    // Curve
    const pts = curves[type];
    ctx.strokeStyle = '#FF3D00';
    ctx.lineWidth = 3;
    ctx.beginPath();
    ctx.moveTo(pts[0][0], pts[0][1]);
    pts.slice(1).forEach((p) => ctx.lineTo(p[0], p[1]));
    ctx.stroke();

    // Points
    ctx.fillStyle = '#F2EFE9';
    pts.forEach((p) => {
      ctx.beginPath();
      ctx.arc(p[0], p[1], 4, 0, Math.PI * 2);
      ctx.fill();
      ctx.strokeStyle = '#0A0A0A';
      ctx.lineWidth = 2;
      ctx.stroke();
    });
  }, [type]);

  return (
    <canvas
      ref={canvasRef}
      width={350}
      height={120}
      className="w-full border-1.5 border-ink bg-paper-2"
    />
  );
}
