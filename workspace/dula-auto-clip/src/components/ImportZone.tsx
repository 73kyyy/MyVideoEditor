import { Upload, Film } from 'lucide-react';

export default function ImportZone() {
  return (
    <div className="border border-dashed border-dim/60 rounded-xl p-8 flex flex-col items-center justify-center gap-4 hover:border-neon/40 transition-colors duration-300 cursor-pointer group">
      <div className="w-16 h-16 rounded-xl bg-surface2 flex items-center justify-center group-hover:bg-neon/10 transition-colors duration-300">
        <Upload className="w-7 h-7 text-muted group-hover:text-neon transition-colors duration-300" />
      </div>
      <div className="text-center">
        <p className="font-grotesk font-medium text-paper mb-1">Drop video files here</p>
        <p className="font-mono text-xs text-muted">
          MP4, MOV, MKV · Up to 4K · Batch import supported
        </p>
      </div>
      <div className="flex items-center gap-2 mt-2">
        <button className="px-4 py-2 rounded-md bg-neon/10 border border-neon/30 text-neon font-grotesk text-sm font-medium hover:bg-neon/20 transition-colors">
          <Film className="w-4 h-4 inline mr-1.5" />
          Browse Files
        </button>
        <button className="px-4 py-2 rounded-md bg-surface2 border border-dim/30 text-muted font-grotesk text-sm font-medium hover:text-paper hover:border-dim/60 transition-colors">
          Paste URL
        </button>
      </div>
    </div>
  );
}
