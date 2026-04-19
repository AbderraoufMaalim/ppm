'use client';

import { useState, FormEvent } from 'react';
import { useRouter } from 'next/navigation';

export default function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const router = useRouter();

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const res = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password }),
      });

      const data = await res.json();

      if (!res.ok) {
        setError(data.error || 'Erreur de connexion');
        return;
      }

      // Succès — rediriger vers le dashboard
      router.push('/');
      router.refresh();
    } catch {
      setError('Impossible de contacter le serveur');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      {/* Animated background orbs */}
      <div className="login-orb login-orb-1" />
      <div className="login-orb login-orb-2" />
      <div className="login-orb login-orb-3" />

      <div className="login-container">
        <div className="login-card">
          {/* Logo / Brand */}
          <div className="login-brand">
            <div className="login-logo">🎮</div>
            <h1 className="login-title">PushPlay</h1>
            <p className="login-subtitle">Manager V2</p>
          </div>

          {/* Form */}
          <form onSubmit={handleSubmit} className="login-form">
            <div className="login-field">
              <label htmlFor="username" className="login-label">
                👤 Nom d&apos;utilisateur
              </label>
              <input
                id="username"
                type="text"
                className="login-input"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="Admin"
                autoComplete="username"
                autoFocus
                required
              />
            </div>

            <div className="login-field">
              <label htmlFor="password" className="login-label">
                🔒 Mot de passe
              </label>
              <input
                id="password"
                type="password"
                className="login-input"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                autoComplete="current-password"
                required
              />
            </div>

            {error && (
              <div className="login-error">
                ❌ {error}
              </div>
            )}

            <button
              type="submit"
              className="login-btn"
              disabled={loading}
            >
              {loading ? (
                <span className="login-spinner" />
              ) : (
                <>▶ Se connecter</>
              )}
            </button>
          </form>

          <div className="login-footer">
            <p>Système de gestion — Cybercafé & Salle de jeux</p>
          </div>
        </div>
      </div>

      <style jsx>{`
        .login-page {
          min-height: 100vh;
          display: flex;
          align-items: center;
          justify-content: center;
          position: relative;
          overflow: hidden;
          background: #0a0a12;
        }

        /* Animated floating orbs */
        .login-orb {
          position: absolute;
          border-radius: 50%;
          filter: blur(80px);
          opacity: 0.5;
          animation: float 8s ease-in-out infinite;
          pointer-events: none;
        }
        .login-orb-1 {
          width: 400px;
          height: 400px;
          background: rgba(108, 99, 255, 0.25);
          top: -10%;
          left: -5%;
          animation-duration: 8s;
        }
        .login-orb-2 {
          width: 300px;
          height: 300px;
          background: rgba(0, 212, 255, 0.15);
          bottom: -10%;
          right: -5%;
          animation-duration: 10s;
          animation-delay: 2s;
        }
        .login-orb-3 {
          width: 200px;
          height: 200px;
          background: rgba(179, 136, 255, 0.2);
          top: 50%;
          left: 60%;
          animation-duration: 12s;
          animation-delay: 4s;
        }

        @keyframes float {
          0%, 100% { transform: translate(0, 0) scale(1); }
          33% { transform: translate(30px, -20px) scale(1.05); }
          66% { transform: translate(-20px, 20px) scale(0.95); }
        }

        .login-container {
          position: relative;
          z-index: 10;
          width: 100%;
          max-width: 420px;
          padding: 20px;
        }

        .login-card {
          background: rgba(255, 255, 255, 0.04);
          border: 1px solid rgba(255, 255, 255, 0.08);
          border-radius: 24px;
          padding: 48px 36px 36px;
          backdrop-filter: blur(24px);
          box-shadow: 0 25px 60px rgba(0, 0, 0, 0.4);
          animation: cardAppear 0.6s ease-out;
        }

        @keyframes cardAppear {
          from { opacity: 0; transform: translateY(30px) scale(0.96); }
          to { opacity: 1; transform: translateY(0) scale(1); }
        }

        .login-brand {
          text-align: center;
          margin-bottom: 36px;
        }

        .login-logo {
          width: 72px;
          height: 72px;
          margin: 0 auto 16px;
          background: linear-gradient(135deg, #6C63FF, #B388FF);
          border-radius: 20px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 36px;
          box-shadow: 0 0 30px rgba(108, 99, 255, 0.35);
          animation: logoPulse 3s ease-in-out infinite;
        }

        @keyframes logoPulse {
          0%, 100% { box-shadow: 0 0 30px rgba(108, 99, 255, 0.35); }
          50% { box-shadow: 0 0 50px rgba(108, 99, 255, 0.5); }
        }

        .login-title {
          font-size: 1.8rem;
          font-weight: 800;
          letter-spacing: -0.03em;
          color: #EAEAEF;
          margin-bottom: 2px;
        }

        .login-subtitle {
          font-size: 0.8rem;
          text-transform: uppercase;
          letter-spacing: 0.2em;
          color: rgba(234, 234, 239, 0.3);
          font-weight: 600;
        }

        .login-form {
          display: flex;
          flex-direction: column;
          gap: 20px;
        }

        .login-field {
          display: flex;
          flex-direction: column;
          gap: 8px;
        }

        .login-label {
          font-size: 0.8rem;
          font-weight: 600;
          color: rgba(234, 234, 239, 0.55);
          letter-spacing: 0.02em;
        }

        .login-input {
          width: 100%;
          padding: 14px 18px;
          background: rgba(255, 255, 255, 0.05);
          border: 1px solid rgba(255, 255, 255, 0.1);
          border-radius: 12px;
          color: #EAEAEF;
          font-size: 0.95rem;
          font-family: 'Inter', sans-serif;
          transition: all 0.25s ease;
          outline: none;
        }

        .login-input::placeholder {
          color: rgba(234, 234, 239, 0.2);
        }

        .login-input:focus {
          border-color: rgba(108, 99, 255, 0.5);
          box-shadow: 0 0 0 3px rgba(108, 99, 255, 0.15);
          background: rgba(255, 255, 255, 0.07);
        }

        .login-error {
          padding: 12px 16px;
          background: rgba(255, 82, 82, 0.1);
          border: 1px solid rgba(255, 82, 82, 0.25);
          border-radius: 10px;
          font-size: 0.85rem;
          color: #FF5252;
          text-align: center;
          animation: shake 0.4s ease;
        }

        @keyframes shake {
          0%, 100% { transform: translateX(0); }
          25% { transform: translateX(-6px); }
          75% { transform: translateX(6px); }
        }

        .login-btn {
          width: 100%;
          padding: 16px;
          background: linear-gradient(135deg, #6C63FF, #7C4DFF);
          border: none;
          border-radius: 12px;
          color: white;
          font-size: 1rem;
          font-weight: 700;
          font-family: 'Inter', sans-serif;
          cursor: pointer;
          transition: all 0.25s ease;
          box-shadow: 0 4px 20px rgba(108, 99, 255, 0.3);
          margin-top: 4px;
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 8px;
          min-height: 52px;
        }

        .login-btn:hover:not(:disabled) {
          transform: translateY(-2px);
          box-shadow: 0 8px 30px rgba(108, 99, 255, 0.45);
        }

        .login-btn:active:not(:disabled) {
          transform: translateY(0);
        }

        .login-btn:disabled {
          opacity: 0.7;
          cursor: not-allowed;
        }

        .login-spinner {
          width: 22px;
          height: 22px;
          border: 3px solid rgba(255, 255, 255, 0.3);
          border-top-color: white;
          border-radius: 50%;
          animation: spin 0.7s linear infinite;
        }

        @keyframes spin {
          to { transform: rotate(360deg); }
        }

        .login-footer {
          text-align: center;
          margin-top: 28px;
          padding-top: 20px;
          border-top: 1px solid rgba(255, 255, 255, 0.06);
        }

        .login-footer p {
          font-size: 0.72rem;
          color: rgba(234, 234, 239, 0.2);
          letter-spacing: 0.03em;
        }
      `}</style>
    </div>
  );
}
