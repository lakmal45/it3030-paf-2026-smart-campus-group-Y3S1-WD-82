import { Link } from "react-router-dom";
import {
  ArrowRight,
  Building2,
  Users,
  ShieldCheck,
  Zap,
  User,
  Settings,
  LogOut,
  ChevronDown,
  CheckCircle2,
  LayoutDashboard,
  Database,
  Activity,
  Target,
  Clock,
  Lock,
  Globe,
  Mail,
  Phone,
  MapPin,
  Send,
  MessageSquare,
  Twitter,
  Facebook,
  Instagram,
  Linkedin,
  Laptop,
  Fingerprint,
  CalendarCheck,
  FileText,
  BellRing,
  CheckSquare,
} from "lucide-react";
import { useAuth } from "../context/AuthContext";
import { useState, useRef, useEffect } from "react";

const Home = () => {
  const { user, logout } = useAuth();
  const [isProfileOpen, setIsProfileOpen] = useState(false);
  const profileDropdownRef = useRef(null);
  const [isScrolled, setIsScrolled] = useState(false);

  // Close profile dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (
        profileDropdownRef.current &&
        !profileDropdownRef.current.contains(event.target)
      ) {
        setIsProfileOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  // Handle scroll for sticky navigation
  useEffect(() => {
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 20);
    };
    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  const handleLogout = () => {
    setIsProfileOpen(false);
    logout();
  };

  const scrollToSection = (id) => {
    const element = document.getElementById(id);
    if (element) {
      element.scrollIntoView({ behavior: "smooth" });
    }
  };

  return (
    <div className="min-h-screen bg-[#F8FAFC] font-sans selection:bg-indigo-500/30 overflow-x-hidden relative">
      {/* Decorative Background Elements */}
      <div className="fixed inset-0 pointer-events-none z-0 overflow-hidden">
        {/* Modern dot grid background */}
        <div className="absolute inset-0 bg-[url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAiIGhlaWdodD0iNDAiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PGNpcmNsZSBjeD0iMSIgY3k9IjEiIHI9IjEiIGZpbGw9InJnYmEoOTksIDEwMiwgMjQxLCAwLjE1KSIvPjwvc3ZnPg==')] bg-[length:40px_40px] opacity-50" />
        
        {/* Subtle geometric shapes instead of heavy blurs */}
        <div className="absolute top-[10%] right-[5%] w-[40vw] h-[40vw] border-[1px] border-indigo-200/40 rounded-full opacity-60 pointer-events-none" />
        <div className="absolute top-[15%] right-[10%] w-[30vw] h-[30vw] border-[1px] border-purple-200/40 rounded-full opacity-60 pointer-events-none" />
        <div className="absolute top-[20%] right-[15%] w-[20vw] h-[20vw] border-[1px] border-cyan-200/40 rounded-full opacity-60 pointer-events-none" />

        <div className="absolute top-[30%] -left-[10%] w-96 h-96 bg-gradient-to-tr from-cyan-100/40 to-indigo-100/40 rounded-full blur-[60px]" />
        <div className="absolute -bottom-[20%] right-[10%] w-96 h-96 bg-gradient-to-bl from-rose-100/40 to-orange-100/40 rounded-full blur-[60px]" />
        
        {/* Scattered interactive elements */}
        <div className="absolute top-[25%] left-[20%] w-3 h-3 bg-indigo-400 rounded-full shadow-[0_0_15px_rgba(99,102,241,0.5)]" />
        <div className="absolute top-[35%] right-[25%] w-2 h-2 bg-purple-400 rounded-full opacity-70" />
        <div className="absolute bottom-[20%] left-[30%] w-4 h-4 bg-cyan-400/50 rounded-full" />
      </div>

      {/* Sticky Navigation Header */}
      <header
        className={`fixed top-0 left-0 right-0 z-50 transition-all duration-300 ${isScrolled ? "bg-white/80 backdrop-blur-xl border-b border-slate-200/50 shadow-sm py-4" : "bg-transparent py-6"}`}
      >
        <div className="max-w-7xl mx-auto px-6 lg:px-12 flex justify-between items-center">
          <div
            className="flex items-center gap-3 group cursor-pointer"
            onClick={() => scrollToSection("hero")}
          >
            <div className="w-12 h-12 rounded-xl overflow-hidden shadow-lg shadow-indigo-500/20 group-hover:scale-105 transition-transform duration-300 bg-white">
              <img
                src="/src/assets/unihub.png"
                alt="UniHub Logo"
                className="w-full h-full object-cover"
              />
            </div>
            <div className="flex flex-col">
              <span className="text-2xl font-bold tracking-tight text-slate-800 bg-clip-text text-transparent bg-gradient-to-r from-indigo-700 to-purple-700">
                UniHub
              </span>
            </div>
          </div>

          <nav className="hidden md:flex items-center gap-8">
            {["About", "Features", "Flow", "Benefits", "Contact"].map(
              (item) => (
                <button
                  key={item}
                  onClick={() => scrollToSection(item.toLowerCase())}
                  className="text-sm font-semibold text-slate-600 hover:text-indigo-600 transition-colors relative group"
                >
                  {item}
                  <span className="absolute -bottom-1 left-0 w-0 h-0.5 bg-indigo-600 transition-all group-hover:w-full"></span>
                </button>
              ),
            )}
          </nav>

          <div className="flex gap-4 items-center">
            {user ? (
              <div className="relative" ref={profileDropdownRef}>
                <button
                  onClick={() => setIsProfileOpen(!isProfileOpen)}
                  className="flex items-center gap-2 p-1.5 pr-3 bg-white/80 backdrop-blur-sm border border-slate-200/80 rounded-full shadow-sm hover:shadow hover:border-slate-300 transition-all focus:outline-none focus:ring-2 focus:ring-indigo-500/20 group"
                >
                  <div className="w-8 h-8 rounded-full bg-indigo-100 flex items-center justify-center overflow-hidden border border-indigo-200 text-indigo-700 font-bold text-sm">
                    {user.name?.charAt(0)?.toUpperCase() || "U"}
                  </div>
                  <ChevronDown
                    size={14}
                    className={`text-slate-500 group-hover:text-slate-700 transition-all duration-300 ${isProfileOpen ? "rotate-180" : ""}`}
                  />
                </button>

                <div
                  className={`absolute right-0 mt-3 w-64 bg-white/95 backdrop-blur-xl rounded-2xl shadow-[0_10px_40px_rgb(0,0,0,0.08)] border border-slate-100 py-2 z-50 transform origin-top-right transition-all duration-200 ${
                    isProfileOpen
                      ? "opacity-100 scale-100 translate-y-0"
                      : "opacity-0 scale-95 -translate-y-2 pointer-events-none"
                  }`}
                >
                  <div className="px-4 py-3 border-b border-slate-100/80">
                    <p className="text-sm font-bold text-slate-800 truncate">
                      {user.name || "User Account"}
                    </p>
                    <p className="text-xs font-semibold text-indigo-500 uppercase tracking-wider mt-1">
                      {user.role || "STUDENT"}
                    </p>
                  </div>
                  <div className="px-2 py-2">
                    <Link
                      to="/dashboard"
                      onClick={() => setIsProfileOpen(false)}
                      className="w-full flex items-center gap-3 px-3 py-2.5 text-sm font-medium text-slate-600 hover:text-indigo-700 hover:bg-indigo-50 rounded-xl transition-all group"
                    >
                      <LayoutDashboard
                        size={16}
                        className="text-slate-400 group-hover:text-indigo-600 transition-colors"
                      />
                      Dashboard
                    </Link>
                    <button className="w-full flex items-center gap-3 px-3 py-2.5 text-sm font-medium text-slate-600 hover:text-indigo-700 hover:bg-indigo-50 rounded-xl transition-all group">
                      <User
                        size={16}
                        className="text-slate-400 group-hover:text-indigo-600 transition-colors"
                      />
                      Profile Settings
                    </button>
                  </div>
                  <div className="px-2 py-2 border-t border-slate-100/80">
                    <button
                      onClick={handleLogout}
                      className="w-full flex items-center gap-3 px-3 py-2.5 text-sm font-semibold text-rose-600 hover:bg-rose-50 rounded-xl transition-all group"
                    >
                      <LogOut
                        size={16}
                        className="text-rose-500 group-hover:text-rose-600 transition-colors"
                      />
                      Sign out
                    </button>
                  </div>
                </div>
              </div>
            ) : (
              <>
                <Link to="/login">
                  <button className="px-5 py-2 text-sm font-semibold text-slate-600 hover:text-indigo-600 transition-colors bg-white/50 hover:bg-white/80 backdrop-blur-md border border-slate-200/50 rounded-full shadow-sm hover:shadow hidden sm:block">
                    Sign In
                  </button>
                </Link>
                <Link to="/signup">
                  <button className="px-5 py-2 text-sm font-semibold text-white bg-indigo-600 hover:bg-indigo-500 transition-all rounded-full shadow-md shadow-indigo-500/20 hover:shadow-indigo-500/40 hover:-translate-y-0.5">
                    Get Started
                  </button>
                </Link>
              </>
            )}
          </div>
        </div>
      </header>

      <main className="pt-24 pb-0">
        {/* Hero Section */}
        <section
          id="hero"
          className="relative z-10 max-w-7xl mx-auto px-6 lg:px-12 pt-24 pb-28 flex flex-col items-center text-center"
        >
          {/* Floating tags */}
          <div className="absolute top-1/4 left-4 lg:left-12 hidden md:flex items-center gap-2 px-4 py-2 bg-white/80 backdrop-blur-md border border-slate-200/50 rounded-2xl shadow-sm transform -rotate-6 hover:rotate-0 hover:-translate-y-1 transition-all duration-300 cursor-default">
            <div className="w-2 h-2 bg-emerald-500 rounded-full shadow-[0_0_8px_rgba(16,185,129,0.8)]"></div>
            <span className="text-xs font-bold text-slate-700 uppercase tracking-wider">Live Sync</span>
          </div>
          <div className="absolute top-1/3 right-4 lg:right-12 hidden md:flex items-center gap-2 px-4 py-2 bg-white/80 backdrop-blur-md border border-slate-200/50 rounded-2xl shadow-sm transform rotate-6 hover:rotate-0 hover:-translate-y-1 transition-all duration-300 cursor-default">
            <div className="w-2 h-2 bg-indigo-500 rounded-full shadow-[0_0_8px_rgba(99,102,241,0.8)]"></div>
            <span className="text-xs font-bold text-slate-700 uppercase tracking-wider">Secure</span>
          </div>
          <div className="absolute bottom-1/4 left-1/4 hidden lg:flex items-center gap-2 px-4 py-2 bg-white/80 backdrop-blur-md border border-slate-200/50 rounded-2xl shadow-sm transform -rotate-3 hover:rotate-0 hover:-translate-y-1 transition-all duration-300 cursor-default">
            <div className="w-2 h-2 bg-purple-500 rounded-full shadow-[0_0_8px_rgba(168,85,247,0.8)]"></div>
            <span className="text-xs font-bold text-slate-700 uppercase tracking-wider">Analytics</span>
          </div>

          <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-indigo-50/80 border border-indigo-100/80 backdrop-blur-sm mb-8 hover:bg-indigo-100 transition-colors cursor-default">
            <span className="relative flex h-2 w-2">
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-indigo-400 opacity-75"></span>
              <span className="relative inline-flex rounded-full h-2 w-2 bg-indigo-600"></span>
            </span>
            <span className="text-sm font-semibold text-indigo-700 uppercase tracking-wider">
              IT3030 PAF 2026 Project
            </span>
          </div>

          <h1 className="text-5xl md:text-7xl lg:text-8xl font-extrabold text-slate-900 tracking-tight leading-[1.1] max-w-5xl mx-auto drop-shadow-sm mb-6">
            Next-Generation <br />
            <span className="text-transparent bg-clip-text bg-gradient-to-r from-indigo-600 via-purple-600 to-cyan-600">
              Campus Operations
            </span>
          </h1>

          <p className="text-lg md:text-xl text-slate-600 font-medium max-w-2xl mx-auto mb-10 leading-relaxed">
            Centralize your facility management, asset tracking, and maintenance
            workflows into one intelligent platform. Let's make your campus
            smarter, together.
          </p>

          <div className="flex flex-col sm:flex-row justify-center gap-4 w-full sm:w-auto">
            {user ? (
              <Link to="/dashboard" className="group w-full sm:w-auto">
                <button className="w-full relative inline-flex items-center justify-center px-8 py-4 text-base font-bold text-white transition-all duration-300 bg-indigo-600 border border-transparent rounded-full shadow-xl shadow-indigo-600/30 hover:bg-indigo-500 hover:-translate-y-1 focus:outline-none focus:ring-2 focus:ring-offset-2 hover:shadow-indigo-600/50 focus:ring-indigo-600 active:scale-95 leading-none">
                  <LayoutDashboard className="w-5 h-5 mr-2" />
                  Go to Dashboard
                  <ArrowRight className="w-5 h-5 ml-2 transition-transform duration-300 group-hover:translate-x-1" />
                </button>
              </Link>
            ) : (
              <>
                <Link to="/signup" className="group w-full sm:w-auto">
                  <button className="w-full relative inline-flex items-center justify-center px-8 py-4 text-base font-bold text-white transition-all duration-300 bg-indigo-600 border border-transparent rounded-full shadow-xl shadow-indigo-600/30 hover:bg-indigo-500 hover:-translate-y-1 focus:outline-none focus:ring-2 focus:ring-offset-2 hover:shadow-indigo-600/50 focus:ring-indigo-600 active:scale-95 leading-none">
                    Create an Account
                    <ArrowRight className="w-5 h-5 ml-2 transition-transform duration-300 group-hover:translate-x-1" />
                  </button>
                </Link>
                <button
                  onClick={() => scrollToSection("about")}
                  className="w-full sm:w-auto inline-flex items-center justify-center px-8 py-4 text-base font-bold text-slate-700 transition-all duration-300 bg-white shadow-xl shadow-slate-200/50 border border-slate-100 rounded-full hover:bg-slate-50 hover:-translate-y-1 hover:text-indigo-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-slate-200 active:scale-95 leading-none group"
                >
                  Learn More
                </button>
              </>
            )}
          </div>
        </section>

        {/* About Section */}
        <section
          id="about"
          className="py-24 bg-white relative z-10 border-t border-slate-100"
        >
          <div className="max-w-7xl mx-auto px-6 lg:px-12 flex flex-col md:flex-row items-center gap-16">
            <div className="flex-1 relative">
              {/* Dotted pattern behind image */}
              <div className="absolute -inset-8 bg-[url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAiIGhlaWdodD0iMjAiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PGNpcmNsZSBjeD0iMSIgY3k9IjEiIHI9IjEiIGZpbGw9InJnYmEoOTksIDEwMiwgMjQxLCAwLjIpIi8+PC9zdmc+')] bg-[length:20px_20px] rounded-[3rem] -z-10"></div>
              <div className="absolute -inset-4 bg-gradient-to-r from-indigo-100 to-purple-100 rounded-[2rem] transform -rotate-3 z-0 transition-transform duration-500 hover:rotate-0"></div>
              <img
                src="https://images.unsplash.com/photo-1522071820081-009f0129c71c?ixlib=rb-4.0.3&auto=format&fit=crop&w=1200&q=80"
                alt="Students collaborating"
                className="relative z-10 rounded-[2rem] shadow-[0_20px_50px_rgba(0,0,0,0.1)] object-cover w-full h-[400px] hover:-translate-y-2 transition-transform duration-500"
              />
              <div className="absolute -bottom-6 -right-6 bg-white/90 backdrop-blur-md p-6 rounded-2xl shadow-[0_10px_30px_rgba(0,0,0,0.08)] border border-white z-20 flex items-center gap-4 hover:-translate-y-2 hover:shadow-[0_20px_40px_rgba(0,0,0,0.12)] transition-all duration-500">
                <div className="w-12 h-12 bg-indigo-50 rounded-xl flex items-center justify-center text-indigo-600">
                  <Target size={24} />
                </div>
                <div>
                  <p className="text-slate-500 text-sm font-semibold uppercase tracking-wider">
                    Project Vision
                  </p>
                  <p className="text-slate-800 font-bold text-lg">
                    IT3030 Excellence
                  </p>
                </div>
              </div>
            </div>
            <div className="flex-1">
              <h2 className="text-indigo-600 font-bold tracking-wider uppercase text-sm mb-3">
                About The Project
              </h2>
              <h3 className="text-3xl md:text-4xl font-bold text-slate-900 mb-6 leading-tight">
                Bridging the gap between campus resources and real-time
                management.
              </h3>
              <p className="text-slate-600 mb-6 leading-relaxed text-lg">
                Developed as a comprehensive solution for the IT3030 Performance
                Architecture framework, the UniHub platform is engineered to
                modernize traditional university operations.
              </p>
              <p className="text-slate-600 mb-8 leading-relaxed">
                By leveraging role-based access, real-time analytics, and
                asynchronous notification systems, we provide a unified
                ecosystem for students, faculty, maintenance staff, and
                administrators to collaborate effectively.
              </p>
              <ul className="space-y-4">
                {[
                  "Microservice-oriented Spring Boot backend",
                  "Responsive React & Tailwind CSS frontend",
                  "Secure JWT authentication and role management",
                ].map((item, idx) => (
                  <li key={idx} className="flex items-center gap-3">
                    <div className="w-6 h-6 rounded-full bg-indigo-100 text-indigo-600 flex items-center justify-center flex-shrink-0">
                      <CheckCircle2 size={14} />
                    </div>
                    <span className="text-slate-700 font-medium">{item}</span>
                  </li>
                ))}
              </ul>
            </div>
          </div>
        </section>

        {/* Features Section */}
        <section
          id="features"
          className="py-24 bg-slate-50 relative z-10 border-t border-slate-100"
        >
          <div className="max-w-7xl mx-auto px-6 lg:px-12">
            <div className="text-center max-w-3xl mx-auto mb-16">
              <h2 className="text-indigo-600 font-bold tracking-wider uppercase text-sm mb-3">
                Core Features
              </h2>
              <h3 className="text-3xl md:text-4xl font-bold text-slate-900 mb-4">
                Everything you need to manage your campus
              </h3>
              <p className="text-lg text-slate-600">
                Powerful modules crafted for different roles ensuring maximum
                efficiency.
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
              {[
                {
                  icon: Building2,
                  title: "Resource Management",
                  desc: "Interactive tracking of all campus facilities, equipment, and real-time room availability.",
                  color: "text-blue-600",
                  bg: "bg-blue-100/50",
                },
                {
                  icon: CalendarCheck,
                  title: "Smart Bookings",
                  desc: "Automated booking system with conflict resolution and instant managerial approval workflows.",
                  color: "text-indigo-600",
                  bg: "bg-indigo-100/50",
                },
                {
                  icon: Zap,
                  title: "Maintenance Ticketing",
                  desc: "Report issues instantly. Track repair progress from initial ticket to final resolution.",
                  color: "text-amber-600",
                  bg: "bg-amber-100/50",
                },
                {
                  icon: ShieldCheck,
                  title: "Secure Access",
                  desc: "Role-based dashboards for Students, Managers, Technicians, and System Administrators.",
                  color: "text-emerald-600",
                  bg: "bg-emerald-100/50",
                },
                {
                  icon: Activity,
                  title: "Real-time Analytics",
                  desc: "Comprehensive dashboards with usage statistics, pending tasks, and historical reports.",
                  color: "text-purple-600",
                  bg: "bg-purple-100/50",
                },
                {
                  icon: Globe,
                  title: "System Notifications",
                  desc: "Stay informed with integrated Email and Push notifications for every critical update.",
                  color: "text-rose-600",
                  bg: "bg-rose-100/50",
                },
              ].map((item, idx) => (
                <div
                  key={idx}
                  className="relative bg-white/70 backdrop-blur-md border border-slate-200/60 p-8 rounded-[2rem] shadow-sm hover:shadow-[0_20px_50px_rgba(79,70,229,0.08)] hover:-translate-y-2 transition-all duration-500 group overflow-hidden"
                >
                  {/* Decorative hover gradient */}
                  <div className={`absolute -top-24 -right-24 w-48 h-48 ${item.bg} rounded-full blur-[40px] opacity-0 group-hover:opacity-50 transition-opacity duration-500 pointer-events-none`}></div>
                  
                  <div
                    className={`relative w-14 h-14 rounded-2xl ${item.bg} ${item.color} flex items-center justify-center mb-6 group-hover:scale-110 group-hover:rotate-3 transition-transform duration-500 shadow-sm border border-white`}
                  >
                    <item.icon size={28} />
                  </div>
                  <h3 className="relative text-xl font-bold text-slate-800 mb-3 group-hover:text-indigo-900 transition-colors">
                    {item.title}
                  </h3>
                  <p className="relative text-slate-500 font-medium leading-relaxed">
                    {item.desc}
                  </p>
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* Operational Flow Section */}
        <section
          id="flow"
          className="py-24 bg-white relative z-10 border-t border-slate-100"
        >
          <div className="max-w-7xl mx-auto px-6 lg:px-12">
            <div className="text-center max-w-3xl mx-auto mb-16">
              <h2 className="text-indigo-600 font-bold tracking-wider uppercase text-sm mb-3">
                How It Works
              </h2>
              <h3 className="text-3xl md:text-4xl font-bold text-slate-900 mb-4">
                Streamlined Operational Flow
              </h3>
              <p className="text-lg text-slate-600">
                A simple, transparent process from request to resolution.
              </p>
            </div>

            <div className="relative">
              {/* Connection Line */}
              <div className="hidden lg:block absolute top-10 left-[10%] w-[80%] h-0.5 bg-gradient-to-r from-transparent via-indigo-200 to-transparent -translate-y-1/2 z-0 border-t-2 border-dashed border-indigo-200"></div>

              <div className="grid grid-cols-1 lg:grid-cols-4 gap-12 relative z-10">
                {[
                  {
                    step: "01",
                    icon: Laptop,
                    title: "Submit Request",
                    desc: "User logs in and submits a resource booking or maintenance ticket via their dashboard.",
                  },
                  {
                    step: "02",
                    icon: BellRing,
                    title: "Automated Routing",
                    desc: "System instantly notifies the appropriate manager or technician via email and app.",
                  },
                  {
                    step: "03",
                    icon: FileText,
                    title: "Review & Action",
                    desc: "Staff reviews the request, approves resources, or begins maintenance work.",
                  },
                  {
                    step: "04",
                    icon: CheckSquare,
                    title: "Resolution",
                    desc: "Task is completed, analytics are updated, and the user is notified of the outcome.",
                  },
                ].map((item, idx) => (
                  <div
                    key={idx}
                    className="flex flex-col items-center text-center relative group"
                  >
                    <div className="w-20 h-20 bg-white border border-slate-200 rounded-2xl transform rotate-3 group-hover:rotate-0 group-hover:-translate-y-2 transition-all duration-500 flex items-center justify-center shadow-[0_8px_30px_rgb(0,0,0,0.06)] mb-6 z-10 overflow-hidden relative">
                      <div className="absolute inset-0 bg-gradient-to-br from-indigo-50 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500"></div>
                      <item.icon size={32} className="text-indigo-600 relative z-10 group-hover:scale-110 transition-transform duration-500" />
                    </div>
                    <div className="absolute top-0 right-0 lg:-top-6 lg:-right-2 text-7xl font-black text-transparent bg-clip-text bg-gradient-to-br from-slate-100 to-slate-50 -z-10 group-hover:from-indigo-50 group-hover:to-transparent transition-all duration-500 transform group-hover:scale-110 group-hover:-translate-y-4">
                      {item.step}
                    </div>
                    <h3 className="text-xl font-bold text-slate-800 mb-3 group-hover:text-indigo-600 transition-colors">
                      {item.title}
                    </h3>
                    <p className="text-slate-500 leading-relaxed text-sm">
                      {item.desc}
                    </p>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </section>

        {/* Benefits Section */}
        <section
          id="benefits"
          className="py-24 bg-slate-900 relative z-10 overflow-hidden"
        >
          {/* Background grid and shapes */}
          <div className="absolute inset-0 bg-[url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAiIGhlaWdodD0iNDAiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PGNpcmNsZSBjeD0iMSIgY3k9IjEiIHI9IjEiIGZpbGw9InJnYmEoMjU1LCAyNTUsIDI1NSwgMC4wNSkiLz48L3N2Zz4=')] bg-[length:40px_40px]" />
          
          <div className="absolute top-0 right-0 w-1/2 h-full bg-gradient-to-l from-indigo-900/30 to-transparent pointer-events-none" />
          <div className="absolute -top-[50%] -right-[10%] w-[80%] h-[150%] bg-gradient-to-b from-indigo-500/10 to-purple-500/10 transform rotate-12 pointer-events-none blur-3xl"></div>

          <div className="max-w-7xl mx-auto px-6 lg:px-12 relative z-10">
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-16 items-center">
              <div>
                <h2 className="text-indigo-400 font-bold tracking-wider uppercase text-sm mb-3">
                  Why Choose Us
                </h2>
                <h3 className="text-3xl md:text-4xl font-bold text-white mb-6 leading-tight">
                  Unlock the full potential of your campus infrastructure.
                </h3>
                <p className="text-slate-300 mb-8 leading-relaxed text-lg">
                  Transitioning to the UniHub system reduces operational
                  bottlenecks, minimizes paperwork, and provides actionable
                  insights for future investments.
                </p>
                <div className="space-y-6">
                  {[
                    {
                      title: "Time Saving Efficiency",
                      desc: "Automate repetitive tasks and reduce approval times by up to 80%.",
                    },
                    {
                      title: "Paperless Operations",
                      desc: "Digitize all records, contributing to a greener, more sustainable campus.",
                    },
                    {
                      title: "Data-Driven Decisions",
                      desc: "Utilize analytics to understand resource utilization and maintenance costs.",
                    },
                  ].map((item, idx) => (
                    <div key={idx} className="flex gap-4">
                      <div className="w-12 h-12 rounded-xl bg-white/10 flex items-center justify-center flex-shrink-0 text-indigo-400">
                        <Target size={24} />
                      </div>
                      <div>
                        <h4 className="text-white font-bold text-lg mb-1">
                          {item.title}
                        </h4>
                        <p className="text-slate-400">{item.desc}</p>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
              <div className="relative group">
                <div className="absolute -inset-1 bg-gradient-to-r from-indigo-500 to-purple-500 rounded-[2.5rem] blur opacity-20 group-hover:opacity-40 transition duration-1000 group-hover:duration-200"></div>
                <div className="relative bg-slate-800/80 backdrop-blur-xl border border-white/10 rounded-[2rem] p-8 shadow-2xl transform group-hover:-translate-y-2 transition-transform duration-500">
                  <div className="flex items-center justify-between mb-8 border-b border-white/10 pb-6">
                    <div>
                      <p className="text-slate-400 text-sm font-medium mb-1">
                        Total Savings
                      </p>
                      <h4 className="text-4xl font-black text-white">
                        45<span className="text-indigo-400">hrs</span>/wk
                      </h4>
                    </div>
                    <div className="w-16 h-16 rounded-full bg-indigo-500/20 flex items-center justify-center text-indigo-400">
                      <Clock size={32} />
                    </div>
                  </div>
                  <div className="space-y-4">
                    <div className="flex justify-between items-center">
                      <span className="text-slate-300">
                        Ticket Resolution Time
                      </span>
                      <span className="text-emerald-400 font-bold">- 65%</span>
                    </div>
                    <div className="w-full bg-slate-700 rounded-full h-2">
                      <div className="bg-emerald-400 h-2 rounded-full w-[65%]"></div>
                    </div>

                    <div className="flex justify-between items-center pt-4">
                      <span className="text-slate-300">
                        Resource Utilization
                      </span>
                      <span className="text-indigo-400 font-bold">+ 85%</span>
                    </div>
                    <div className="w-full bg-slate-700 rounded-full h-2">
                      <div className="bg-indigo-400 h-2 rounded-full w-[85%]"></div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* Contact Section */}
        <section id="contact" className="py-24 bg-slate-50 relative z-10 border-t border-slate-100 overflow-hidden">
          {/* Decorative contact background */}
          <div className="absolute top-0 left-0 w-full h-full pointer-events-none opacity-[0.03] bg-[radial-gradient(#4f46e5_1px,transparent_1px)] [background-size:20px_20px]"></div>
          
          <div className="max-w-7xl mx-auto px-6 lg:px-12 relative z-10">
            <div className="text-center max-w-3xl mx-auto mb-16">
              <h2 className="text-indigo-600 font-bold tracking-wider uppercase text-sm mb-3">
                Get in Touch
              </h2>
              <h3 className="text-3xl md:text-4xl font-bold text-slate-900 mb-4">
                Have questions? We're here to help.
              </h3>
              <p className="text-lg text-slate-600">
                Reach out to our support team for any inquiries or technical
                assistance.
              </p>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-5 gap-12 bg-slate-50 rounded-[3rem] p-8 md:p-12 border border-slate-100">
              <div className="lg:col-span-2 space-y-8">
                <div>
                  <h4 className="text-2xl font-bold text-slate-800 mb-6">
                    Contact Information
                  </h4>
                  <p className="text-slate-600 mb-8 leading-relaxed">
                    Our team is available Monday through Friday, 9:00 AM to 5:00
                    PM to assist you with the UniHub platform.
                  </p>
                </div>

                <div className="space-y-6">
                  <div className="flex items-center gap-4">
                    <div className="w-12 h-12 bg-white rounded-xl shadow-sm border border-slate-100 flex items-center justify-center text-indigo-600 flex-shrink-0">
                      <Mail size={20} />
                    </div>
                    <div>
                      <p className="text-sm font-semibold text-slate-500">
                        Email Us
                      </p>
                      <p className="font-medium text-slate-800">
                        support@unihub.edu
                      </p>
                    </div>
                  </div>
                  <div className="flex items-center gap-4">
                    <div className="w-12 h-12 bg-white rounded-xl shadow-sm border border-slate-100 flex items-center justify-center text-indigo-600 flex-shrink-0">
                      <Phone size={20} />
                    </div>
                    <div>
                      <p className="text-sm font-semibold text-slate-500">
                        Call Us
                      </p>
                      <p className="font-medium text-slate-800">
                        +1 (555) 123-4567
                      </p>
                    </div>
                  </div>
                  <div className="flex items-center gap-4">
                    <div className="w-12 h-12 bg-white rounded-xl shadow-sm border border-slate-100 flex items-center justify-center text-indigo-600 flex-shrink-0">
                      <MapPin size={20} />
                    </div>
                    <div>
                      <p className="text-sm font-semibold text-slate-500">
                        Location
                      </p>
                      <p className="font-medium text-slate-800">
                        IT Building, Main Campus
                      </p>
                    </div>
                  </div>
                </div>
              </div>

              <div className="lg:col-span-3">
                <form
                  className="bg-white p-8 rounded-3xl shadow-[0_8px_30px_rgb(0,0,0,0.04)] border border-slate-100"
                  onSubmit={(e) => e.preventDefault()}
                >
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
                    <div>
                      <label className="block text-sm font-semibold text-slate-700 mb-2">
                        First Name
                      </label>
                      <input
                        type="text"
                        className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all bg-slate-50 focus:bg-white"
                        placeholder="John"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-semibold text-slate-700 mb-2">
                        Last Name
                      </label>
                      <input
                        type="text"
                        className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all bg-slate-50 focus:bg-white"
                        placeholder="Doe"
                      />
                    </div>
                  </div>
                  <div className="mb-6">
                    <label className="block text-sm font-semibold text-slate-700 mb-2">
                      Email Address
                    </label>
                    <input
                      type="email"
                      className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all bg-slate-50 focus:bg-white"
                      placeholder="john@example.com"
                    />
                  </div>
                  <div className="mb-6">
                    <label className="block text-sm font-semibold text-slate-700 mb-2">
                      Message
                    </label>
                    <textarea
                      rows="4"
                      className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all bg-slate-50 focus:bg-white resize-none"
                      placeholder="How can we help you?"
                    ></textarea>
                  </div>
                  <button
                    type="submit"
                    className="w-full flex items-center justify-center gap-2 px-6 py-4 bg-indigo-600 hover:bg-indigo-500 text-white font-bold rounded-xl transition-all shadow-md shadow-indigo-500/20 hover:shadow-indigo-500/40 hover:-translate-y-0.5"
                  >
                    <Send size={18} />
                    Send Message
                  </button>
                </form>
              </div>
            </div>
          </div>
        </section>
      </main>

      {/* Rich Footer */}
      <footer className="bg-slate-900 pt-20 pb-10 relative z-10 border-t border-slate-800">
        <div className="max-w-7xl mx-auto px-6 lg:px-12">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-12 gap-12 mb-16">
            <div className="lg:col-span-4">
              <div className="flex items-center gap-3 mb-6">
                <div className="w-12 h-12 rounded-xl overflow-hidden bg-white">
                  <img
                    src="/src/assets/unihub.png"
                    alt="UniHub Logo"
                    className="w-full h-full object-cover"
                  />
                </div>
                <span className="text-white font-bold text-2xl tracking-tight">
                  UniHub
                </span>
              </div>
              <p className="text-slate-400 mb-8 leading-relaxed pr-4">
                Empowering universities with intelligent digital infrastructure
                to manage resources, streamline maintenance, and foster
                collaboration.
              </p>
              <div className="flex gap-4">
                {[Twitter, Facebook, Instagram, Linkedin].map((Icon, idx) => (
                  <button
                    key={idx}
                    className="w-10 h-10 rounded-full bg-slate-800 flex items-center justify-center text-slate-400 hover:bg-indigo-500 hover:text-white transition-all"
                  >
                    <Icon size={18} />
                  </button>
                ))}
              </div>
            </div>

            <div className="lg:col-span-2">
              <h4 className="text-white font-bold mb-6">Quick Links</h4>
              <ul className="space-y-4">
                {["Home", "About Us", "Features", "Pricing", "Contact"].map(
                  (item) => (
                    <li key={item}>
                      <button
                        onClick={() =>
                          item === "Home"
                            ? scrollToSection("hero")
                            : scrollToSection(item.split(" ")[0].toLowerCase())
                        }
                        className="text-slate-400 hover:text-indigo-400 transition-colors text-sm"
                      >
                        {item}
                      </button>
                    </li>
                  ),
                )}
              </ul>
            </div>

            <div className="lg:col-span-2">
              <h4 className="text-white font-bold mb-6">Resources</h4>
              <ul className="space-y-4">
                {[
                  "Documentation",
                  "Help Center",
                  "API Reference",
                  "System Status",
                ].map((item) => (
                  <li key={item}>
                    <Link
                      to="#"
                      className="text-slate-400 hover:text-indigo-400 transition-colors text-sm"
                    >
                      {item}
                    </Link>
                  </li>
                ))}
              </ul>
            </div>

            <div className="lg:col-span-4">
              <h4 className="text-white font-bold mb-6">
                Subscribe to Newsletter
              </h4>
              <p className="text-slate-400 text-sm mb-4">
                Get the latest updates and announcements directly to your inbox.
              </p>
              <form className="flex gap-2" onSubmit={(e) => e.preventDefault()}>
                <input
                  type="email"
                  placeholder="Enter your email"
                  className="flex-1 px-4 py-3 rounded-xl bg-slate-800 border border-slate-700 text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all"
                />
                <button
                  type="submit"
                  className="px-6 py-3 bg-indigo-600 hover:bg-indigo-500 text-white font-bold rounded-xl transition-all shadow-md"
                >
                  Subscribe
                </button>
              </form>
            </div>
          </div>

          <div className="pt-8 border-t border-slate-800 flex flex-col md:flex-row justify-between items-center gap-4">
            <p className="text-slate-500 text-sm">
              © 2026 Smart Campus Operations. IT3030 PAF Group Project.
            </p>
            <div className="flex gap-6 text-sm">
              <Link
                to="#"
                className="text-slate-500 hover:text-white transition-colors"
              >
                Privacy Policy
              </Link>
              <Link
                to="#"
                className="text-slate-500 hover:text-white transition-colors"
              >
                Terms of Service
              </Link>
              <Link
                to="#"
                className="text-slate-500 hover:text-white transition-colors"
              >
                Cookie Settings
              </Link>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default Home;
