import { CheckCircle, Clock, Loader2, AlertCircle } from 'lucide-react';
import type { ExportTask } from '@/types';

interface ExportQueueProps {
  tasks: ExportTask[];
}

const statusIcons: Record<string, { icon: typeof CheckCircle; color: string }> = {
  done: { icon: CheckCircle, color: 'text-neon' },
  exporting: { icon: Loader2, color: 'text-violet' },
  queued: { icon: Clock, color: 'text-muted' },
  failed: { icon: AlertCircle, color: 'text-heat' },
};

const platformLabels: Record<string, string> = {
  tiktok: 'TikTok',
  reels: 'Reels',
  shorts: 'Shorts',
  custom: 'Custom',
};

export default function ExportQueue({ tasks }: ExportQueueProps) {
  return (
    <div className="space-y-2">
      {tasks.map((task) => {
        const status = statusIcons[task.status];
        const Icon = status.icon;
        return (
          <div
            key={task.id}
            className="glass-light rounded-lg border border-dim/30 p-3 flex items-center gap-3"
          >
            <Icon className={`w-4 h-4 ${status.color} ${task.status === 'exporting' ? 'animate-spin' : ''}`} />
            <div className="flex-1 min-w-0">
              <div className="flex items-center gap-2 mb-1">
                <span className="font-grotesk text-xs font-medium text-paper">
                  {platformLabels[task.platform]}
                </span>
                <span className="font-mono text-[9px] text-muted">
                  {task.quality} · {task.format.toUpperCase()}
                </span>
              </div>
              {task.status === 'exporting' && (
                <div className="h-1 bg-surface3 rounded-full overflow-hidden">
                  <div
                    className="h-full bg-gradient-to-r from-violet to-neon rounded-full transition-all duration-500"
                    style={{ width: `${task.progress}%` }}
                  />
                </div>
              )}
            </div>
            <span className="font-mono text-[10px] text-muted">
              {task.status === 'done' ? '100%' : task.status === 'exporting' ? `${task.progress}%` : 'Queued'}
            </span>
          </div>
        );
      })}
    </div>
  );
}
