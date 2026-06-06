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
        paper: '#F2EFE9',
        'paper-2': '#E8E3D8',
        ink: '#0A0A0A',
        'ink-2': '#3A3A38',
        'ink-3': '#7A786F',
        accent: '#FF3D00',
        'accent-2': '#FFD600',
        green: '#1A6B3F',
      },
      fontFamily: {
        fraunces: ['Fraunces', 'serif'],
        mono: ['JetBrains Mono', 'monospace'],
        body: ['Inter Tight', 'sans-serif'],
      },
      borderWidth: {
        '1.5': '1.5px',
      },
      animation: {
        pulse: 'pulse 1.4s infinite',
      },
      keyframes: {
        pulse: {
          '0%, 100%': { opacity: '1' },
          '50%': { opacity: '0.3' },
        },
      },
    },
  },
  plugins: [],
};
