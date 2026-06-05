import { useState, useEffect } from "react";
import { useNavigate, Link, useLocation } from "react-router-dom";
import api from "../services/api";
import { useAuth } from "../context/AuthContext";

const Login = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const location = useLocation();
  const navigate = useNavigate();
  const { user, login } = useAuth();

  // Make sure 'from' never accidentally loops back to /login
  const from =
    location.state?.from?.pathname === "/login"
      ? "/dashboard"
      : location.state?.from?.pathname || "/dashboard";

  // Watch for the user state to populate, then navigate safely
  useEffect(() => {
    if (user) {
      navigate(from, { replace: true });
    }
  }, [user, navigate, from]);

  const handleLogin = async (e) => {
    e.preventDefault();
    setIsLoading(true);

    try {
      const res = await api.post("/auth/login", { email, password });

      console.log("🟢 Backend Response Data:", res.data);

      if (res.data && res.data.role) {
        // Force 'ADMIN' role for the primary admin email to ensure UI consistency
        const effectiveRole = res.data.email === "admin@campus.com" ? "ADMIN" : res.data.role;
        
        // Explicitly set role for simple checks in components
        localStorage.setItem("role", effectiveRole);
        
        // Update the user object with the effective role before logging in
        const userToLogin = { ...res.data, role: effectiveRole };
        login(userToLogin);
      } else {
        alert("Login failed: The server did not return user details.");
      }
    } catch (err) {
      alert("Invalid email or password");
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  const handleGoogleLogin = () => {
    window.location.href = "http://localhost:8081/oauth2/authorization/google";
  };

  return (
    <div className="min-h-screen flex bg-[#F8FAFC] font-sans selection:bg-indigo-500/30 relative overflow-hidden">
      {/* Decorative Background Elements */}
      <div className="absolute top-0 left-0 w-full h-full overflow-hidden pointer-events-none z-0">
        <div className="absolute -top-[10%] -right-[5%] w-[40%] h-[40%] rounded-full bg-gradient-to-br from-indigo-300/30 to-purple-300/30 blur-[80px] animate-[pulse_8s_ease-in-out_infinite]" />
        <div className="absolute top-[40%] -left-[10%] w-[50%] h-[50%] rounded-full bg-gradient-to-tr from-cyan-300/30 to-blue-300/30 blur-[100px] animate-[pulse_10s_ease-in-out_infinite_alternate]" />
        <div className="absolute -bottom-[20%] right-[10%] w-[40%] h-[40%] rounded-full bg-gradient-to-bl from-rose-200/30 to-orange-200/30 blur-[80px]" />
      </div>

      <div className="w-full flex flex-col justify-center px-6 sm:px-12 lg:px-24 relative overflow-y-auto py-12 z-10">
        <div className="w-full max-w-md mx-auto relative z-10">
          <div className="text-center mb-8">
            <Link to="/" className="flex justify-center items-center gap-3 group inline-flex">
              <div className="w-12 h-12 rounded-xl overflow-hidden shadow-md shadow-indigo-500/10 group-hover:scale-105 transition-transform duration-300 bg-white">
                <img
                  src="/src/assets/unihub.png"
                  alt="UniHub Logo"
                  className="w-full h-full object-cover"
                />
              </div>
              <div className="flex flex-col text-left">
                <span className="text-3xl font-extrabold tracking-tight text-slate-800 bg-clip-text text-transparent bg-gradient-to-r from-indigo-700 to-purple-700 leading-none">
                  UniHub
                </span>
              </div>
            </Link>
            <h2 className="text-3xl font-bold text-slate-900 tracking-tight mt-2">
              Welcome back
            </h2>
            <p className="mt-2 text-sm text-slate-500">
              Please enter your details to sign in
            </p>
          </div>

          <div className="bg-white p-8 shadow-xl shadow-slate-200/40 rounded-3xl border border-slate-100/60">
            <div className="mb-6">
              <button
                onClick={handleGoogleLogin}
                className="w-full flex justify-center items-center gap-3 py-3.5 px-4 border border-slate-200/80 rounded-xl shadow-sm text-sm font-bold text-slate-700 bg-white hover:bg-slate-50 hover:border-slate-300 transition-all duration-300 active:scale-[0.98]"
              >
                <img
                  src="https://www.svgrepo.com/show/475656/google-color.svg"
                  alt="Google"
                  className="w-5 h-5"
                />
                Sign in with Google
              </button>
            </div>

            <div className="relative mb-6">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-slate-200/80"></div>
              </div>
              <div className="relative flex justify-center text-sm">
                <span className="px-3 bg-white text-slate-400 font-medium">
                  Or continue with email
                </span>
              </div>
            </div>

            <form className="space-y-5" onSubmit={handleLogin}>
              <div>
                <label className="block text-sm font-semibold text-slate-700 mb-1.5">
                  Email address
                </label>
                <input
                  type="email"
                  required
                  className="block w-full px-4 py-3.5 bg-slate-50 border border-slate-200/80 rounded-xl focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all text-slate-800 font-medium"
                  placeholder="university@example.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                />
              </div>

              <div>
                <div className="flex items-center justify-between mb-1.5">
                  <label className="block text-sm font-semibold text-slate-700">
                    Password
                  </label>
                  <a href="#" className="text-sm font-semibold text-indigo-600 hover:text-indigo-500">
                    Forgot password?
                  </a>
                </div>
                <input
                  type="password"
                  required
                  className="block w-full px-4 py-3.5 bg-slate-50 border border-slate-200/80 rounded-xl focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all text-slate-800 font-medium tracking-wide"
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                />
              </div>

              <button
                type="submit"
                disabled={isLoading}
                className="w-full py-4 px-4 rounded-xl text-white font-bold bg-indigo-600 hover:bg-indigo-500 shadow-lg shadow-indigo-500/30 hover:shadow-indigo-500/40 hover:-translate-y-0.5 transition-all duration-300 disabled:opacity-70 disabled:hover:translate-y-0 disabled:shadow-none flex justify-center items-center mt-2"
              >
                {isLoading ? "Authenticating..." : "Sign in to account"}
              </button>
            </form>

            <div className="mt-8 text-center">
              <p className="text-sm font-medium text-slate-600">
                New to the system?{" "}
                <Link
                  to="/signup"
                  className="font-bold text-indigo-600 hover:text-indigo-500 underline decoration-indigo-200 underline-offset-4"
                >
                  Create an account
                </Link>
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;
