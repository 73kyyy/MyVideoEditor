interface ColorSliderProps {
  label: string;
  value: string;
  fillWidth: string;
  fillColor: string;
}

export default function ColorSlider({ label, value, fillWidth, fillColor }: ColorSliderProps) {
  return (
    <div className="mb-3.5">
      <div className="flex justify-between mb-1.5">
        <span className="font-mono text-[10px] font-bold tracking-wider">{label}</span>
        <span className="font-mono text-[11px] font-bold">{value}</span>
      </div>
      <div className="h-1.5 bg-paper-2 border border-ink relative">
        <div className="absolute left-0 top-0 bottom-0" style={{ width: fillWidth, background: fillColor }} />
        <div
          className="absolute top-1/2 w-3.5 h-3.5 bg-paper border-2 border-ink -translate-y-1/2"
          style={{ left: fillWidth }}
        />
      </div>
    </div>
  );
}
