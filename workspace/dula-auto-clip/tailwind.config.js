/** @type {import('tailwindcss').Config} */
export default {
  darkMode: "class",
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    container: {
      center: true,
    },
    extend: {
      colors: {
        void: '#0D0D0F',
        surface: '#16161A',
        surface2: '#1E1E24',
        surface3: '#28282F',
        neon: '#00F0FF',
        neon2: '#00C4D4',
        violet: '#8B5CF6',
        violet2: '#7C3AED',
        heat: '#FF6B35',
        heat2: '#FF8F5E',
        paper: '#E8E6E3',
        muted: '#6B6B76',
        dim: '#3A3A44',
      },
      fontFamily: {
        grotesk: ['"Space Grotesk"', 'sans-serif'],
        mono: ['"IBM Plex Mono"', 'monospace'],
        body: ['"DM Sans"', 'sans-serif'],
      },
      borderWidth: {
        1.5: '1.5px',
      },
      boxShadow: {
        neon: '0 0 12px rgba(0, 240, 255, 0.3)',
        violet: '0 0 12px rgba(139, 92, 246, 0.3)',
        heat: '0 0 12px rgba(255, 107, 53, 0.3)',
        glass: '0 8px 32px rgba(0, 0, 0, 0.4)',
      },
      animation: {
        pulse2: 'pulse2 1.4s ease-in-out infinite',
        shimmer: 'shimmer 2s linear infinite',
        glow: 'glow 2s ease-in-out infinite',
      },
      keyframes: {
        pulse2: {
          '0%, 100%': { opacity: '1' },
          '50%': { opacity: '0.4' },
        },
        shimmer: {
          '0%': { backgroundPosition: '-200% 0' },
          '100%': { backgroundPosition: '200% 0' },
        },
        glow: {
          '0%, 100%': { boxShadow: '0 0 8px rgba(0, 240, 255, 0.2)' },
          '50%': { boxShadow: '0 0 20px rgba(0, 240, 255, 0.5)' },
        },
      },
    },
  },
  plugins: [],
};
