import { Brain, Heart, Activity, Eye } from 'lucide-react';
import GlassPanel from './GlassPanel';
import { useAIStore } from '@/stores/aiStore';

export default function AIAnalysisCard() {
  const { analysis } = useAIStore();

  const isAnalyzing = analysis.status === 'analyzing';

  return (
    <GlassPanel neon className="p-5">
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-2">
          <Brain className="w-5 h-5 text-neon" />
          <span className="font-grotesk font-semibold text-paper">AI Analysis</span>
        </div>
        <div className={`flex items-center gap-1.5 font-mono text-[10px] tracking-wider ${
          isAnalyzing ? 'text-heat' : 'text-neon'
        }`}>
          <span className={`w-1.5 h-1.5 rounded-full ${isAnalyzing ? 'bg-heat animate-pulse2' : 'bg-neon'}`} />
          {isAnalyzing ? 'ANALYZING' : 'COMPLETE'}
        </div>
      </div>

      {/* Progress Bar */}
      {isAnalyzing && (
        <div className="h-1.5 bg-surface3 rounded-full mb-4 overflow-hidden">
          <div
            className="h-full bg-gradient-to-r from-neon to-violet rounded-full transition-all duration-500"
            style={{ width: `${analysis.progress}%` }}
          />
        </div>
      )}

      {/* Stats Grid */}
      <div className="grid grid-cols-3 gap-3">
        <div className="bg-surface/60 rounded-lg p-3 text-center border border-dim/20">
          <Eye className="w-4 h-4 text-violet mx-auto mb-1.5" />
          <div className="font-grotesk font-bold text-lg text-paper">{analysis.sceneCount}</div>
          <div className="font-mono text-[9px] text-muted tracking-wider uppercase">Scenes</div>
        </div>
        <div className="bg-surface/60 rounded-lg p-3 text-center border border-dim/20">
          <Heart className="w-4 h-4 text-heat mx-auto mb-1.5" />
          <div className="font-grotesk font-bold text-lg text-paper">{analysis.emotionPeaks}</div>
          <div className="font-mono text-[9px] text-muted tracking-wider uppercase">Emotions</div>
        </div>
        <div className="bg-surface/60 rounded-lg p-3 text-center border border-dim/20">
          <Activity className="w-4 h-4 text-neon mx-auto mb-1.5" />
          <div className="font-grotesk font-bold text-lg text-paper">{analysis.rhythmBeats}</div>
          <div className="font-mono text-[9px] text-muted tracking-wider uppercase">Beats</div>
        </div>
      </div>

      {/* Quick Actions */}
      <div className="flex gap-2 mt-4">
        <button className="flex-1 py-2 rounded-md bg-neon/10 border border-neon/30 text-neon font-grotesk text-xs font-medium hover:bg-neon/20 transition-colors">
          Re-Analyze
        </button>
        <button className="flex-1 py-2 rounded-md bg-violet/10 border border-violet/30 text-violet font-grotesk text-xs font-medium hover:bg-violet/20 transition-colors">
          Adjust Params
        </button>
      </div>
    </GlassPanel>
  );
}
