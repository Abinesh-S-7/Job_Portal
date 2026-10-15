const API_BASE = "http://localhost:8080/api";

// Active session state
let currentUser = JSON.parse(localStorage.getItem("user")) || null;
let currentToken = localStorage.getItem("token") || null;

document.addEventListener("DOMContentLoaded", () => {
  syncNavState();
  initDateTimePicker();
  showView("home");
});

function initDateTimePicker() {
  const deadlineInput = document.getElementById("jobDeadline");
  if (deadlineInput) {
    const now = new Date();
    now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
    deadlineInput.min = now.toISOString().slice(0, 16);
  }
}

// Global View Switcher
function showView(viewId) {
  // Recruiters cannot view the public browse job list
  if (viewId === "browse" && currentUser?.role === "RECRUITER") {
    viewId = "my-jobs";
  }

  document.querySelectorAll(".view").forEach((v) => v.classList.add("hidden"));
  const target = document.getElementById(`view-${viewId}`);
  if (target) target.classList.remove("hidden");

  if (viewId === "home") renderHeroActions();
  if (viewId === "browse") loadJobs();
  if (viewId === "my-jobs") loadRecruiterJobs();
  if (viewId === "my-applications") loadCandidateApplications();
  if (viewId === "candidate-profile") loadCandidateProfile();
  if (viewId === "company-profile") loadCompanyProfile();
  if (viewId === "admin-dashboard") loadAdminDashboard();
}

function renderHeroActions() {
  const container = document.getElementById("heroActionButtons");
  if (!container) return;

  if (currentToken && currentUser) {
    if (currentUser.role === "RECRUITER") {
      container.innerHTML = `
        <button class="btn btn-primary" onclick="showView('my-jobs')">Go to Recruiter Dashboard</button>
        <button class="btn btn-secondary" onclick="showView('post-job')">+ Post an Opening</button>
      `;
    } else if (currentUser.role === "ADMIN") {
      container.innerHTML = `
        <button class="btn btn-primary" onclick="showView('admin-dashboard')">Open Admin Dashboard</button>
      `;
    } else {
      container.innerHTML = `
        <button class="btn btn-primary" onclick="showView('browse')">Browse Jobs</button>
        <button class="btn btn-secondary" onclick="showView('my-applications')">View My Applications</button>
      `;
    }
  } else {
    container.innerHTML = `
      <button class="btn btn-primary" onclick="showView('register')">Get Started Free</button>
      <button class="btn btn-secondary" onclick="showView('login')">Sign In to Account</button>
      <button class="btn btn-outline" onclick="showView('browse')">Browse Openings</button>
    `;
  }
}

function syncNavState() {
  const isAuth = !!currentToken;
  const isRecruiter = isAuth && currentUser?.role === "RECRUITER";
  const isCandidate = isAuth && currentUser?.role === "CANDIDATE";
  const isAdmin = isAuth && currentUser?.role === "ADMIN";

  document.querySelectorAll(".auth-in").forEach((el) => el.style.display = isAuth ? "inline-block" : "none");
  document.querySelectorAll(".auth-out").forEach((el) => el.style.display = isAuth ? "none" : "inline-block");
  document.querySelectorAll(".recruiter-only").forEach((el) => el.style.display = isRecruiter ? "inline-block" : "none");
  document.querySelectorAll(".candidate-only").forEach((el) => el.style.display = isCandidate ? "inline-block" : "none");
  document.querySelectorAll(".admin-only").forEach((el) => el.style.display = isAdmin ? "inline-block" : "none");
  document.querySelectorAll(".non-recruiter").forEach((el) => el.style.display = (isRecruiter || isAdmin) ? "none" : "inline-block");

  const badge = document.getElementById("navUserBadge");
  if (isAuth && currentUser) {
    badge.textContent = `${currentUser.name} (${currentUser.role})`;
  }
}

function showAlert(message, type = "success") {
  const banner = document.getElementById("alertBanner");
  banner.textContent = message;
  banner.className = `alert-banner ${type}`;
  banner.classList.remove("hidden");
  setTimeout(() => banner.classList.add("hidden"), 4000);
}

async function apiCall(endpoint, method = "GET", body = null) {
  const headers = { "Content-Type": "application/json" };
  if (currentToken) {
    headers["Authorization"] = `Bearer ${currentToken}`;
  }

  const config = { method, headers };
  if (body) config.body = JSON.stringify(body);

  const res = await fetch(`${API_BASE}${endpoint}`, config);
  const data = await res.json().catch(() => null);

  if (!res.ok) {
    throw new Error(data?.message || `HTTP ${res.status}: Action failed`);
  }
  return data;
}

/* ==========================================================================
   AUTHENTICATION MODULE
   ========================================================================== */

async function handleLogin(e) {
  e.preventDefault();
  const email = document.getElementById("loginEmail").value.trim();
  const password = document.getElementById("loginPassword").value.trim();

  try {
    const data = await apiCall("/auth/login", "POST", { email, password });
    currentToken = data.token;
    currentUser = { id: data.id, name: data.name, email: data.email, role: data.role };

    localStorage.setItem("token", currentToken);
    localStorage.setItem("user", JSON.stringify(currentUser));

    showAlert(`Welcome back, ${data.name}!`);
    syncNavState();

    if (data.role === "RECRUITER") {
      showView("my-jobs");
    } else if (data.role === "ADMIN") {
      showView("admin-dashboard");
    } else {
      showView("home");
    }
  } catch (err) {
    showAlert(err.message, "error");
  }
}

async function handleRegister(e) {
  e.preventDefault();
  const selectedRole = document.querySelector('input[name="regRole"]:checked')?.value || "CANDIDATE";

  const payload = {
    name: document.getElementById("regName").value.trim(),
    email: document.getElementById("regEmail").value.trim(),
    password: document.getElementById("regPassword").value.trim(),
    role: selectedRole
  };

  try {
    const data = await apiCall("/auth/register", "POST", payload);
    currentToken = data.token;
    currentUser = { id: data.id, name: data.name, email: data.email, role: data.role };

    localStorage.setItem("token", currentToken);
    localStorage.setItem("user", JSON.stringify(currentUser));

    showAlert("Account created successfully!");
    syncNavState();

    if (data.role === "RECRUITER") {
      showView("my-jobs");
    } else {
      showView("home");
    }
  } catch (err) {
    showAlert(err.message, "error");
  }
}

function handleLogout() {
  localStorage.removeItem("token");
  localStorage.removeItem("user");
  currentToken = null;
  currentUser = null;
  syncNavState();
  showAlert("Signed out successfully.");
  showView("home");
}

/* ==========================================================================
   PUBLIC & CANDIDATE JOB EXPLORATION
   ========================================================================== */

async function loadJobs() {
  const title = document.getElementById("searchTitle").value.trim();
  const location = document.getElementById("searchLocation").value.trim();

  let query = "";
  const params = new URLSearchParams();
  if (title) params.append("title", title);
  if (location) params.append("location", location);
  if (params.toString()) query = `?${params.toString()}`;

  try {
    const jobs = await apiCall(`/jobs${query}`);
    const container = document.getElementById("jobList");
    container.innerHTML = "";

    if (!jobs || jobs.length === 0) {
      container.innerHTML = "<p style='color: var(--text-muted);'>No matching job openings found.</p>";
      return;
    }

    let appliedJobMap = new Map();
    if (currentToken && currentUser?.role === "CANDIDATE") {
      try {
        const myApps = await apiCall("/applications/my");
        if (Array.isArray(myApps)) {
          myApps.forEach((app) => appliedJobMap.set(app.jobId, app.status));
        }
      } catch (e) {
        console.warn("Could not load user applications map", e);
      }
    }

    jobs.forEach((job) => {
      const isCandidate = currentUser?.role === "CANDIDATE";
      const hasApplied = appliedJobMap.has(job.id);
      const appStatus = appliedJobMap.get(job.id);

      let actionButtonHtml = "";
      if (isCandidate) {
        if (hasApplied) {
          actionButtonHtml = `<button class="btn btn-applied full-width" disabled>✓ Applied (${appStatus})</button>`;
        } else {
          actionButtonHtml = `<button class="btn btn-primary full-width" onclick="applyForJob(${job.id})">Apply Now</button>`;
        }
      } else if (!currentToken) {
        actionButtonHtml = `<button class="btn btn-outline full-width" onclick="showView('login')">Sign In to Apply</button>`;
      }

      const card = document.createElement("div");
      card.className = "card";
      card.innerHTML = `
        <div>
          <h3 class="card-title">${job.title}</h3>
          <p class="card-subtitle">${job.companyName || "Verified Partner"} &bull; ${job.location}</p>
          <p class="card-desc">${job.description}</p>
          <div class="tag-list">
            ${(job.requiredSkills || []).map((s) => `<span class="tag">${s}</span>`).join("")}
          </div>
          <div class="card-meta">
            <span><strong>Experience:</strong> ${job.experienceRequired}+ yrs</span>
            <span><strong>Salary:</strong> ₹${Number(job.salary).toLocaleString()}</span>
            <span><strong>Type:</strong> ${job.jobType}</span>
            <span><strong>Deadline:</strong> ${new Date(job.deadline).toLocaleDateString()}</span>
          </div>
        </div>
        <div style="margin-top: 1rem;">
          ${actionButtonHtml}
        </div>
      `;
      container.appendChild(card);
    });
  } catch (err) {
    showAlert(err.message, "error");
  }
}

async function applyForJob(jobId) {
  try {
    await apiCall("/applications", "POST", { jobId });
    showAlert("Application submitted successfully!");
    loadJobs();
  } catch (err) {
    showAlert(err.message, "error");
  }
}

/* ==========================================================================
   RECRUITER DASHBOARD
   ========================================================================== */

async function loadRecruiterJobs() {
  try {
    const company = await apiCall("/company/me");
    const allJobs = await apiCall("/jobs");
    const myJobs = (allJobs || []).filter((j) => j.companyId === company.id);

    const container = document.getElementById("recruiterJobList");
    container.innerHTML = "";

    if (!myJobs || myJobs.length === 0) {
      container.innerHTML = `
        <div style="grid-column: 1 / -1; text-align: center; padding: 3rem 1rem;">
          <p style="color: var(--text-muted); margin-bottom: 1rem;">You have not published any job openings yet.</p>
          <button class="btn btn-primary" onclick="showView('post-job')">+ Create Your First Job</button>
        </div>
      `;
      return;
    }

    myJobs.forEach((job) => {
      const card = document.createElement("div");
      card.className = "card";
      card.innerHTML = `
        <div>
          <h3 class="card-title">${job.title}</h3>
          <p class="card-subtitle">${company.companyName} &bull; ${job.location}</p>
          <p class="card-desc">${job.description}</p>
          <div class="tag-list">
            ${(job.requiredSkills || []).map((s) => `<span class="tag">${s}</span>`).join("")}
          </div>
          <div class="card-meta">
            <span><strong>Experience:</strong> ${job.experienceRequired}+ yrs</span>
            <span><strong>Salary:</strong> ₹${Number(job.salary).toLocaleString()}</span>
            <span><strong>Type:</strong> ${job.jobType}</span>
            <span><strong>Deadline:</strong> ${new Date(job.deadline).toLocaleDateString()}</span>
          </div>
        </div>
        <div style="display: flex; flex-direction: column; gap: 0.5rem; margin-top: 1rem;">
          <button class="btn btn-primary full-width" onclick="viewApplicants(${job.id})">View Applicants</button>
          <div style="display: flex; gap: 0.5rem;">
            <button class="btn btn-secondary full-width" onclick='openEditJobModal(${JSON.stringify(job)})'>Edit</button>
            <button class="btn btn-outline full-width" style="color: var(--danger); border-color: #fca5a5;" onclick="deleteJobByRecruiter(${job.id})">Delete</button>
          </div>
        </div>
      `;
      container.appendChild(card);
    });
  } catch (err) {
    showAlert(err.message, "error");
  }
}

async function handlePostJob(e) {
  e.preventDefault();

  const deadlineValue = document.getElementById("jobDeadline").value;
  if (!deadlineValue) {
    showAlert("Please select a valid deadline date and time.", "error");
    return;
  }

  const formattedDeadline = deadlineValue.length === 16 ? `${deadlineValue}:00` : deadlineValue;

  const payload = {
    title: document.getElementById("jobTitle").value.trim(),
    jobType: document.getElementById("jobType").value,
    location: document.getElementById("jobLocation").value.trim(),
    salary: parseFloat(document.getElementById("jobSalary").value) || 0,
    experienceRequired: parseInt(document.getElementById("jobExp").value) || 0,
    deadline: formattedDeadline,
    requiredSkills: document.getElementById("jobSkills").value.split(",").map((s) => s.trim()).filter(Boolean),
    description: document.getElementById("jobDescription").value.trim()
  };

  try {
    await apiCall("/jobs", "POST", payload);
    showAlert("Job opening published successfully!");
    showView("my-jobs");
  } catch (err) {
    showAlert(err.message, "error");
  }
}

/* ==========================================================================
   RECRUITER APPLICANT PIPELINE (FIXED MODAL VIEW)
   ========================================================================== */

async function viewApplicants(jobId) {
  const modal = document.getElementById("applicantsModal");
  const modalTitle = document.getElementById("modalTitle");
  const body = document.getElementById("modalBody");

  modalTitle.textContent = "Candidate Applications";
  body.innerHTML = `
    <div style="text-align: center; padding: 2.5rem; color: var(--text-muted);">
      Loading candidate details...
    </div>`;
  modal.classList.remove("hidden");

  try {
    const applicants = await apiCall(`/applications/job/${jobId}`);

    if (!applicants || applicants.length === 0) {
      body.innerHTML = `
        <div style="text-align: center; padding: 2.5rem; color: var(--text-muted);">
          No candidates have applied to this opening yet.
        </div>`;
      return;
    }

    if (applicants[0]?.jobTitle) {
      modalTitle.textContent = `Applicants for "${applicants[0].jobTitle}"`;
    }

    body.innerHTML = "";

    applicants.forEach((app) => {
      const skillsArray = app.skills ? Array.from(app.skills) : [];
      const initials = (app.candidateName || "C")
        .split(" ")
        .map((n) => n[0])
        .slice(0, 2)
        .join("")
        .toUpperCase();

      const card = document.createElement("div");
      card.className = "applicant-card";
      card.innerHTML = `
        <div class="applicant-top">
          <div class="applicant-identity">
            <div class="applicant-avatar">${initials}</div>
            <div>
              <div class="applicant-name-row">
                <h4 class="applicant-name">${app.candidateName || `Candidate #${app.candidateId}`}</h4>
                <span class="status-badge status-${app.status}">${app.status}</span>
              </div>
              <p class="applicant-meta-sub">
                <span>${app.candidateEmail || "No email"}</span> &bull; 
                <span>Applied ${new Date(app.appliedDate).toLocaleDateString()}</span>
              </p>
            </div>
          </div>
        </div>

        <div class="applicant-info-grid">
          <div class="info-cell">
            <span class="cell-label">Education</span>
            <span class="cell-val">${app.education || "Not specified"}</span>
          </div>
          <div class="info-cell">
            <span class="cell-label">Experience</span>
            <span class="cell-val">${app.experience !== null && app.experience !== undefined ? app.experience + " yrs" : "0 yrs"}</span>
          </div>
          <div class="info-cell">
            <span class="cell-label">Phone</span>
            <span class="cell-val">${app.candidatePhone || "Not specified"}</span>
          </div>
          <div class="info-cell">
            <span class="cell-label">Candidate ID</span>
            <span class="cell-val">#${app.candidateId}</span>
          </div>
        </div>

        <div class="applicant-skills-box">
          <span class="cell-label" style="margin-bottom: 0.35rem; display: block;">Skills</span>
          <div class="tag-list">
            ${
              skillsArray.length > 0
                ? skillsArray.map((s) => `<span class="tag">${s}</span>`).join("")
                : '<span class="cell-val" style="color: var(--text-muted); font-size: 0.8rem;">No skills listed</span>'
            }
          </div>
        </div>

        <div class="applicant-bottom-bar">
          <div>
            ${
              app.resumeUrl
                ? `<a href="${app.resumeUrl}" target="_blank" rel="noopener noreferrer" class="btn btn-secondary btn-sm resume-btn">
                    📄 View Resume
                  </a>`
                : `<span class="no-resume">No Resume Attached</span>`
            }
          </div>

          <div class="status-action-group">
            <select id="statusSelect-${app.id}" class="status-select">
              <option value="APPLIED" ${app.status === "APPLIED" ? "selected" : ""}>APPLIED</option>
              <option value="SHORTLISTED" ${app.status === "SHORTLISTED" ? "selected" : ""}>SHORTLISTED</option>
              <option value="INTERVIEW" ${app.status === "INTERVIEW" ? "selected" : ""}>INTERVIEW</option>
              <option value="SELECTED" ${app.status === "SELECTED" ? "selected" : ""}>SELECTED</option>
              <option value="REJECTED" ${app.status === "REJECTED" ? "selected" : ""}>REJECTED</option>
            </select>
            <button class="btn btn-primary btn-sm" onclick="updateApplicantStatus(${app.id})">Update Status</button>
          </div>
        </div>
      `;
      body.appendChild(card);
    });
  } catch (err) {
    body.innerHTML = `
      <div style="text-align: center; padding: 2.5rem; color: var(--danger);">
        Failed to load applicants: ${err.message}
      </div>`;
  }
}

async function updateApplicantStatus(appId) {
  const status = document.getElementById(`statusSelect-${appId}`).value;
  try {
    await apiCall(`/applications/${appId}/status`, "PUT", { status });
    showAlert(`Status updated to ${status}!`);
  } catch (err) {
    showAlert(err.message, "error");
  }
}

function closeApplicantsModal() {
  document.getElementById("applicantsModal").classList.add("hidden");
}

/* ==========================================================================
   EDIT & DELETE JOB LOGIC
   ========================================================================== */

function openEditJobModal(job) {
  document.getElementById("editJobId").value = job.id;
  document.getElementById("editJobTitle").value = job.title;
  document.getElementById("editJobType").value = job.jobType;
  document.getElementById("editJobLocation").value = job.location;
  document.getElementById("editJobSalary").value = job.salary;
  document.getElementById("editJobExp").value = job.experienceRequired;

  if (job.deadline) {
    document.getElementById("editJobDeadline").value = job.deadline.slice(0, 16);
  }

  document.getElementById("editJobSkills").value = (job.requiredSkills || []).join(", ");
  document.getElementById("editJobDescription").value = job.description;

  document.getElementById("editJobModal").classList.remove("hidden");
}

function closeEditJobModal() {
  document.getElementById("editJobModal").classList.add("hidden");
}

async function handleUpdateJob(e) {
  e.preventDefault();
  const id = document.getElementById("editJobId").value;
  const deadlineValue = document.getElementById("editJobDeadline").value;
  const formattedDeadline = deadlineValue.length === 16 ? `${deadlineValue}:00` : deadlineValue;

  const payload = {
    title: document.getElementById("editJobTitle").value.trim(),
    jobType: document.getElementById("editJobType").value,
    location: document.getElementById("editJobLocation").value.trim(),
    salary: parseFloat(document.getElementById("editJobSalary").value) || 0,
    experienceRequired: parseInt(document.getElementById("editJobExp").value) || 0,
    deadline: formattedDeadline,
    requiredSkills: document.getElementById("editJobSkills").value.split(",").map((s) => s.trim()).filter(Boolean),
    description: document.getElementById("editJobDescription").value.trim()
  };

  try {
    await apiCall(`/jobs/${id}`, "PUT", payload);
    showAlert("Job listing updated successfully!");
    closeEditJobModal();
    loadRecruiterJobs();
  } catch (err) {
    showAlert(err.message, "error");
  }
}

async function deleteJobByRecruiter(jobId) {
  if (!confirm("Are you sure you want to delete this job? All related applications will be removed.")) return;
  try {
    await apiCall(`/jobs/${jobId}`, "DELETE");
    showAlert("Job post deleted successfully.");
    loadRecruiterJobs();
  } catch (err) {
    showAlert(err.message, "error");
  }
}

/* ==========================================================================
   CANDIDATE APPLICATIONS & PROFILE
   ========================================================================== */

async function loadCandidateApplications() {
  try {
    const apps = await apiCall("/applications/my");
    const container = document.getElementById("applicationsTableContainer");

    if (!apps || apps.length === 0) {
      container.innerHTML = "<p style='padding: 2rem; color: var(--text-muted);'>You have not submitted any applications yet.</p>";
      return;
    }

    container.innerHTML = `
      <table>
        <thead>
          <tr>
            <th>Position Title</th>
            <th>Company</th>
            <th>Submission Date</th>
            <th>Current Status</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          ${apps.map((app) => `
            <tr>
              <td><strong>${app.jobTitle}</strong></td>
              <td>${app.companyName}</td>
              <td>${new Date(app.appliedDate).toLocaleDateString()}</td>
              <td><span class="status-badge status-${app.status}">${app.status}</span></td>
              <td>
                ${app.status !== "WITHDRAWN" ? `<button class="btn btn-outline" style="padding: 0.35rem 0.75rem; font-size: 0.8rem;" onclick="withdrawApplication(${app.id})">Withdraw</button>` : `<span style="color: var(--text-muted); font-size: 0.85rem;">Withdrawn</span>`}
              </td>
            </tr>
          `).join("")}
        </tbody>
      </table>
    `;
  } catch (err) {
    showAlert(err.message, "error");
  }
}

async function withdrawApplication(appId) {
  if (!confirm("Are you sure you want to withdraw this application?")) return;
  try {
    await apiCall(`/applications/${appId}/withdraw`, "PUT");
    showAlert("Application marked as withdrawn.");
    loadCandidateApplications();
  } catch (err) {
    showAlert(err.message, "error");
  }
}

async function loadCandidateProfile() {
  try {
    const profile = await apiCall("/candidate-profile/me");
    document.getElementById("candEducation").value = profile.education || "";
    document.getElementById("candExperience").value = profile.experience || 0;
    document.getElementById("candLocation").value = profile.location || "";
    document.getElementById("candPhone").value = profile.phoneNumber || "";
    document.getElementById("candResume").value = profile.resumeUrl || "";
    document.getElementById("candAbout").value = profile.about || "";
    document.getElementById("candSkills").value = profile.skills ? Array.from(profile.skills).join(", ") : "";
  } catch (err) {
    showAlert(err.message, "error");
  }
}

async function handleUpdateCandidateProfile(e) {
  e.preventDefault();
  const payload = {
    education: document.getElementById("candEducation").value.trim(),
    experience: parseInt(document.getElementById("candExperience").value) || 0,
    location: document.getElementById("candLocation").value.trim(),
    phoneNumber: document.getElementById("candPhone").value.trim(),
    resumeUrl: document.getElementById("candResume").value.trim(),
    about: document.getElementById("candAbout").value.trim(),
    skills: document.getElementById("candSkills").value.split(",").map((s) => s.trim()).filter(Boolean)
  };

  try {
    await apiCall("/candidate-profile/me", "PUT", payload);
    showAlert("Profile and skills saved successfully!");
  } catch (err) {
    showAlert(err.message, "error");
  }
}

/* ==========================================================================
   RECRUITER COMPANY PROFILE
   ========================================================================== */

async function loadCompanyProfile() {
  try {
    const comp = await apiCall("/company/me");
    document.getElementById("compName").value = comp.companyName || "";
    document.getElementById("compLocation").value = comp.location || "";
    document.getElementById("compWebsite").value = comp.website || "";
    document.getElementById("compDesc").value = comp.description || "";
  } catch (err) {
    showAlert(err.message, "error");
  }
}

async function handleUpdateCompany(e) {
  e.preventDefault();
  const payload = {
    companyName: document.getElementById("compName").value.trim(),
    location: document.getElementById("compLocation").value.trim(),
    website: document.getElementById("compWebsite").value.trim(),
    description: document.getElementById("compDesc").value.trim()
  };

  try {
    await apiCall("/company/me", "PUT", payload);
    showAlert("Company profile updated successfully!");
  } catch (err) {
    showAlert(err.message, "error");
  }
}

/* ==========================================================================
   ADMIN DASHBOARD WITH MODERATION
   ========================================================================== */

async function loadAdminDashboard() {
  try {
    const metrics = await apiCall("/admin/metrics");
    document.getElementById("metricUsers").textContent = metrics.totalUsers || 0;
    document.getElementById("metricJobs").textContent = metrics.totalJobs || 0;
    document.getElementById("metricApps").textContent = metrics.totalApplications || 0;

    const users = await apiCall("/admin/users");
    const userContainer = document.getElementById("adminUsersTableContainer");
    userContainer.innerHTML = `
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Email</th>
            <th>Role</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          ${(users || []).map((u) => `
            <tr>
              <td>#${u.id}</td>
              <td><strong>${u.name}</strong></td>
              <td>${u.email}</td>
              <td><span class="tag">${u.role}</span></td>
              <td>
                ${u.role !== 'ADMIN' 
                  ? `<button class="btn btn-outline" style="color: var(--danger); border-color: #fca5a5; padding: 0.3rem 0.6rem; font-size: 0.8rem;" onclick="deleteUserByAdmin(${u.id})">Remove User</button>`
                  : '<span style="color: var(--text-muted); font-size: 0.8rem;">Protected</span>'
                }
              </td>
            </tr>
          `).join("")}
        </tbody>
      </table>
    `;

    const jobs = await apiCall("/jobs");
    const jobsContainer = document.getElementById("adminJobsTableContainer");
    jobsContainer.innerHTML = `
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Title</th>
            <th>Company</th>
            <th>Location</th>
            <th>Deadline</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          ${(jobs || []).map((j) => `
            <tr>
              <td>#${j.id}</td>
              <td><strong>${j.title}</strong></td>
              <td>${j.companyName || 'N/A'}</td>
              <td>${j.location}</td>
              <td>${new Date(j.deadline).toLocaleDateString()}</td>
              <td>
                <button class="btn btn-outline" style="color: var(--danger); border-color: #fca5a5; padding: 0.3rem 0.6rem; font-size: 0.8rem;" 
                      onclick="deleteJobByAdmin(${j.id})">Remove Job</button>
              </td>
            </tr>
          `).join("")}
        </tbody>
      </table>
    `;
  } catch (err) {
    showAlert(err.message, "error");
  }
}

async function deleteUserByAdmin(userId) {
  if (!confirm(`Are you sure you want to delete user #${userId}? All linked listings and applications will be deleted.`)) return;
  try {
    await apiCall(`/admin/users/${userId}`, "DELETE");
    showAlert(`User #${userId} deleted successfully.`);
    loadAdminDashboard();
  } catch (err) {
    showAlert(err.message, "error");
  }
}

async function deleteJobByAdmin(jobId) {
  if (!confirm(`Are you sure you want to delete job #${jobId}?`)) return;
  try {
    await apiCall(`/admin/jobs/${jobId}`, "DELETE");
    showAlert(`Job #${jobId} removed from the platform.`);
    loadAdminDashboard();
  } catch (err) {
    showAlert(err.message, "error");
  }
}