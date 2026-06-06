export default function StatusBar() {
  return (
    <div className="flex justify-between items-center px-6 pt-3.5 pb-1.5 font-mono text-xs font-medium flex-shrink-0 border-b-1.5 border-ink">
      <span className="font-bold">9:41</span>
      <div className="flex gap-2 items-center">
        <span>●●●●</span>
        <span className="w-2 h-2 bg-ink rounded-full" />
        <span>5G</span>
        <span className="w-2 h-2 bg-accent rounded-full animate-pulse" />
        <span>87%</span>
      </div>
    </div>
  );
}
