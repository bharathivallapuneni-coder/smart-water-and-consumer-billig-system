/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        ink: {
          DEFAULT: '#0B1E27',
          50: '#EAF0F1',
          100: '#CBDADC',
          400: '#41616B',
          600: '#1D3A44',
          700: '#132831',
          800: '#0F1F27',
          900: '#0B1E27'
        },
        foam: {
          DEFAULT: '#F4F9F9',
          100: '#FFFFFF',
          200: '#EEF5F5',
          300: '#E1EDED'
        },
        flow: {
          DEFAULT: '#4AA8D8',
          100: '#DFF1F9',
          300: '#7EC2E3',
          500: '#4AA8D8',
          600: '#3C8EB7',
          700: '#2D7698',
          900: '#1A4560'
        },
        amber: {
          DEFAULT: '#E8A33D',
          100: '#FBEACB'
        },
        coral: {
          DEFAULT: '#E15B4F',
          100: '#FBDAD6'
        },
        slate: {
          DEFAULT: '#4A5D63'
        }
      },
      fontFamily: {
        display: ['"Space Grotesk"', 'sans-serif'],
        body: ['"Inter"', 'sans-serif'],
        mono: ['"IBM Plex Mono"', 'monospace']
      },
      boxShadow: {
        panel: '0 1px 2px rgba(11,30,39,0.04), 0 8px 24px -12px rgba(11,30,39,0.15)',
        'panel-lg': '0 20px 40px -20px rgba(11,30,39,0.35)'
      },
      backgroundImage: {
        'flow-gradient': 'linear-gradient(135deg, #4AA8D8 0%, #2D7698 100%)',
        'ink-gradient': 'linear-gradient(160deg, #132831 0%, #0B1E27 100%)'
      },
      keyframes: {
        ripple: {
          '0%': { transform: 'scale(0)', opacity: 0.45 },
          '100%': { transform: 'scale(2.8)', opacity: 0 }
        },
        wave: {
          '0%': { backgroundPositionX: '0px' },
          '100%': { backgroundPositionX: '400px' }
        },
        rise: {
          '0%': { height: '0%' },
          '100%': { height: 'var(--fill, 50%)' }
        },
        drip: {
          '0%,100%': { transform: 'translateY(0)' },
          '50%': { transform: 'translateY(4px)' }
        }
      },
      animation: {
        ripple: 'ripple 0.6s ease-out',
        wave: 'wave 3s linear infinite',
        rise: 'rise 1.1s cubic-bezier(0.22,1,0.36,1) forwards',
        drip: 'drip 2.4s ease-in-out infinite'
      }
    }
  },
  plugins: []
}
