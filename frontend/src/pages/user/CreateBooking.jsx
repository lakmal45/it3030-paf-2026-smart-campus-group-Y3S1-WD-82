import React, { useState, useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { motion, AnimatePresence } from "framer-motion";
import { Calendar, Clock, MapPin, AlignLeft, Send, AlertCircle, ChevronLeft } from "lucide-react";
import bookingService from "../../services/bookingService";
import resourceService from "../../services/resourceService";

// ErrorPopup from previous integrations
const ErrorPopup = ({ message, onClose }) => {
  return (
    <AnimatePresence>
      <motion.div
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        exit={{ opacity: 0, y: -20 }}
        className="fixed top-4 left-1/2 -translate-x-1/2 z-50 flex items-center gap-3 bg-red-600 text-white px-6 py-4 rounded-2xl shadow-2xl"
      >
        <AlertCircle size={24} />
        <div>
          <h4 className="font-bold">Booking Conflict</h4>
          <p className="text-sm opacity-90">{message}</p>
        </div>
        <button
          onClick={onClose}
          className="ml-4 p-1 hover:bg-red-700 rounded-full transition-colors"
        >
          &times;
        </button>
      </motion.div>
    </AnimatePresence>
  );
};

const CreateBooking = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [resources, setResources] = useState([]);
  const [formData, setFormData] = useState({ 
    resource: location.state?.resourceName || "", 
    date: "", 
    startTime: "", 
    endTime: "", 
    reason: "" 
  });
  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState("");
  const [isLoadingResources, setIsLoadingResources] = useState(true);
  const [showErrorPopup, setShowErrorPopup] = useState(false);
  const [popupMessage, setPopupMessage] = useState("");

  useEffect(() => {
    const fetchResources = async () => {
      try {
        const data = await resourceService.getAllResources();
        setResources(data.filter(r => r.status === 'ACTIVE' && r.available));
      } catch (err) {
        console.error("Error fetching resources:", err);
      } finally {
        setIsLoadingResources(false);
      }
    };
    fetchResources();
  }, []);

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const resourceParam = params.get("resource");
    if (resourceParam) {
      setFormData(prev => ({ ...prev, resource: resourceParam }));
    }
  }, [location]);

  const validateForm = () => {
    let newErrors = {};
    if (!formData.resource) newErrors.resource = "Resource is required.";
    if (!formData.date) newErrors.date = "Date is required.";
    if (!formData.startTime) newErrors.startTime = "Start time is required.";
    if (!formData.endTime) newErrors.endTime = "End time is required.";
    if (formData.startTime && formData.endTime && formData.endTime <= formData.startTime) {
      newErrors.endTime = "End time must be after start time.";
    }
    if (!formData.reason.trim()) newErrors.reason = "Reason is required.";
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    setSubmitting(true);
    setSubmitError("");
    setShowErrorPopup(false);

    try {
      await bookingService.createBooking(formData);
      navigate("/dashboard/user/bookings");
    } catch (err) {
      const msg =
        err.response?.data?.message ||
        err.response?.data?.error ||
        "Failed to create booking. Please try again.";
        
      if (msg.toLowerCase().includes("conflict") || msg.toLowerCase().includes("overlap")) {
        setPopupMessage(msg);
        setShowErrorPopup(true);
        setTimeout(() => setShowErrorPopup(false), 5000);
      } else {
        setSubmitError(msg);
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="min-h-[85vh] bg-slate-50/50 p-6 md:p-8 flex items-center justify-center">
      {showErrorPopup && (
        <ErrorPopup message={popupMessage} onClose={() => setShowErrorPopup(false)} />
      )}
      
      <div className="w-full max-w-3xl">
        <motion.div 
          initial={{ opacity: 0, x: -20 }}
          animate={{ opacity: 1, x: 0 }}
          className="flex items-center gap-4 mb-8"
        >
          <button 
            onClick={() => navigate("/dashboard/user/bookings")}
            className="p-2 hover:bg-slate-200 rounded-full transition-colors text-slate-500 hover:text-slate-700 bg-white shadow-sm border border-slate-100"
          >
            <ChevronLeft size={24} />
          </button>
          <div>
            <h1 className="text-3xl font-extrabold text-slate-800 tracking-tight">Create Booking</h1>
            <p className="text-slate-500 mt-1">Reserve campus resources quickly and easily.</p>
          </div>
        </motion.div>

        <motion.div 
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
          className="bg-white rounded-3xl shadow-xl shadow-slate-200/40 border border-slate-100 overflow-hidden"
        >
          <div className="h-2 w-full bg-gradient-to-r from-indigo-500 via-purple-500 to-pink-500"></div>
          
          <div className="p-8">
            {submitError && (
              <motion.div 
                initial={{ opacity: 0, height: 0 }}
                animate={{ opacity: 1, height: "auto" }}
                className="mb-6 p-4 bg-red-50 border border-red-100 text-red-600 rounded-2xl flex items-center gap-3"
              >
                <AlertCircle size={20} className="text-red-500 flex-shrink-0" />
                <p className="text-sm font-medium">{submitError}</p>
              </motion.div>
            )}

            <form onSubmit={handleSubmit} className="space-y-6" noValidate>
              
              <div className="space-y-2">
                <label className="flex items-center gap-2 text-sm font-semibold text-slate-700">
                  <MapPin size={18} className="text-indigo-500" />
                  Select Resource
                </label>
                <div className="relative">
                  <select
                    className={`w-full pl-4 pr-10 py-3.5 bg-slate-50 border-2 rounded-2xl appearance-none transition-colors focus:ring-4 focus:ring-indigo-500/20 outline-none
                      ${errors.resource ? 'border-red-300 focus:border-red-500 text-red-700' : 'border-slate-200 focus:border-indigo-500 text-slate-700'}`}
                    value={formData.resource}
                    onChange={(e) => { setFormData({ ...formData, resource: e.target.value }); if (errors.resource) setErrors({ ...errors, resource: '' }); }}
                    disabled={isLoadingResources}
                  >
                    <option value="" disabled>{isLoadingResources ? "Loading resources..." : "Choose a resource to book"}</option>
                    {resources.map(r => (
                      <option key={r.id} value={r.name}>{r.name} {r.location ? `— ${r.location}` : ''}</option>
                    ))}
                  </select>
                  <div className="absolute right-4 top-1/2 -translate-y-1/2 pointer-events-none text-slate-400">
                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 9l-7 7-7-7"></path></svg>
                  </div>
                </div>
                {errors.resource && <p className="text-red-500 text-sm flex items-center gap-1 mt-1"><AlertCircle size={14}/> {errors.resource}</p>}
              </div>

              <div className="space-y-2">
                <label className="flex items-center gap-2 text-sm font-semibold text-slate-700">
                  <Calendar size={18} className="text-indigo-500" />
                  Date
                </label>
                <input
                  type="date"
                  min={new Date().toISOString().split('T')[0]}
                  className={`w-full px-4 py-3.5 bg-slate-50 border-2 rounded-2xl transition-colors focus:ring-4 focus:ring-indigo-500/20 outline-none
                    ${errors.date ? 'border-red-300 focus:border-red-500 text-red-700' : 'border-slate-200 focus:border-indigo-500 text-slate-700'}`}
                  value={formData.date}
                  onChange={(e) => { setFormData({ ...formData, date: e.target.value }); if (errors.date) setErrors({ ...errors, date: '' }); }}
                />
                {errors.date && <p className="text-red-500 text-sm flex items-center gap-1 mt-1"><AlertCircle size={14}/> {errors.date}</p>}
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-2">
                  <label className="flex items-center gap-2 text-sm font-semibold text-slate-700">
                    <Clock size={18} className="text-indigo-500" />
                    Start Time
                  </label>
                  <input
                    type="time"
                    className={`w-full px-4 py-3.5 bg-slate-50 border-2 rounded-2xl transition-colors focus:ring-4 focus:ring-indigo-500/20 outline-none
                      ${errors.startTime ? 'border-red-300 focus:border-red-500 text-red-700' : 'border-slate-200 focus:border-indigo-500 text-slate-700'}`}
                    value={formData.startTime}
                    onChange={(e) => { setFormData({ ...formData, startTime: e.target.value }); if (errors.startTime) setErrors({ ...errors, startTime: '' }); if (errors.endTime && errors.endTime.includes('after')) setErrors({ ...errors, endTime: '' }); }}
                  />
                  {errors.startTime && <p className="text-red-500 text-sm flex items-center gap-1 mt-1"><AlertCircle size={14}/> {errors.startTime}</p>}
                </div>

                <div className="space-y-2">
                  <label className="flex items-center gap-2 text-sm font-semibold text-slate-700">
                    <Clock size={18} className="text-indigo-500" />
                    End Time
                  </label>
                  <input
                    type="time"
                    className={`w-full px-4 py-3.5 bg-slate-50 border-2 rounded-2xl transition-colors focus:ring-4 focus:ring-indigo-500/20 outline-none
                      ${errors.endTime ? 'border-red-300 focus:border-red-500 text-red-700' : 'border-slate-200 focus:border-indigo-500 text-slate-700'}`}
                    value={formData.endTime}
                    onChange={(e) => { setFormData({ ...formData, endTime: e.target.value }); if (errors.endTime) setErrors({ ...errors, endTime: '' }); }}
                  />
                  {errors.endTime && <p className="text-red-500 text-sm flex items-center gap-1 mt-1"><AlertCircle size={14}/> {errors.endTime}</p>}
                </div>
              </div>

              <div className="space-y-2">
                <label className="flex items-center gap-2 text-sm font-semibold text-slate-700">
                  <AlignLeft size={18} className="text-indigo-500" />
                  Reason for Booking
                </label>
                <textarea
                  rows="4"
                  className={`w-full px-4 py-3.5 bg-slate-50 border-2 rounded-2xl transition-colors focus:ring-4 focus:ring-indigo-500/20 outline-none resize-none
                    ${errors.reason ? 'border-red-300 focus:border-red-500 text-red-700' : 'border-slate-200 focus:border-indigo-500 text-slate-700'}`}
                  placeholder="E.g., Group study session for Physics 101..."
                  value={formData.reason}
                  onChange={(e) => { setFormData({ ...formData, reason: e.target.value }); if (errors.reason) setErrors({ ...errors, reason: '' }); }}
                ></textarea>
                {errors.reason && <p className="text-red-500 text-sm flex items-center gap-1 mt-1"><AlertCircle size={14}/> {errors.reason}</p>}
              </div>

              <div className="pt-4">
                <motion.button
                  whileHover={{ scale: 1.01 }}
                  whileTap={{ scale: 0.98 }}
                  type="submit"
                  disabled={submitting}
                  className="w-full py-4 bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-700 hover:to-purple-700 disabled:from-indigo-400 disabled:to-purple-400 text-white font-bold rounded-2xl shadow-lg shadow-indigo-200 flex items-center justify-center gap-3 transition-all"
                >
                  {submitting ? (
                    <>
                      <svg className="animate-spin -ml-1 mr-2 h-5 w-5 text-white" fill="none" viewBox="0 0 24 24">
                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                      </svg>
                      Processing...
                    </>
                  ) : (
                    <>
                      <Send size={20} />
                      Confirm Booking
                    </>
                  )}
                </motion.button>
              </div>
            </form>
          </div>
        </motion.div>
      </div>
    </div>
  );
};

export default CreateBooking;


