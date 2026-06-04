/* =========================================================
   SSC CGL EXAM PORTAL — RESULT JS
   Chart.js initializations and review filtering
   ========================================================= */

'use strict';

document.addEventListener('DOMContentLoaded', () => {
    initSectionPieChart();
    initSectionBarChart();
    initReviewFilters();
    animateScoreCircle();
});

// ============================================================
// PIE CHART — Question Distribution
// ============================================================
function initSectionPieChart() {
    const canvas = document.getElementById('sectionPieChart');
    if (!canvas) return;

    const dataEl = document.getElementById('pie-chart-data');
    if (!dataEl) return;

    let data;
    try {
        data = JSON.parse(dataEl.textContent);
    } catch (e) {
        console.error('Failed to parse pie chart data', e);
        return;
    }

    new Chart(canvas, {
        type: 'doughnut',
        data: {
            labels: data.labels,
            datasets: [{
                data: data.values,
                backgroundColor: ['#17a2b8', '#28a745', '#ffc107', '#dc3545'],
                borderColor: '#fff',
                borderWidth: 3,
                hoverOffset: 8
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        padding: 16,
                        font: { size: 12, weight: '600' },
                        usePointStyle: true
                    }
                },
                tooltip: {
                    callbacks: {
                        label: ctx => ` ${ctx.label}: ${ctx.parsed} questions`
                    }
                }
            },
            cutout: '60%'
        }
    });
}

// ============================================================
// BAR CHART — Section-wise Score
// ============================================================
function initSectionBarChart() {
    const canvas = document.getElementById('sectionBarChart');
    if (!canvas) return;

    const dataEl = document.getElementById('bar-chart-data');
    if (!dataEl) return;

    let data;
    try {
        data = JSON.parse(dataEl.textContent);
    } catch (e) {
        console.error('Failed to parse bar chart data', e);
        return;
    }

    new Chart(canvas, {
        type: 'bar',
        data: {
            labels: data.labels,
            datasets: [
                {
                    label: 'Correct',
                    data: data.correct,
                    backgroundColor: 'rgba(40, 167, 69, 0.8)',
                    borderColor: '#28a745',
                    borderWidth: 1.5,
                    borderRadius: 4
                },
                {
                    label: 'Incorrect',
                    data: data.incorrect,
                    backgroundColor: 'rgba(220, 53, 69, 0.8)',
                    borderColor: '#dc3545',
                    borderWidth: 1.5,
                    borderRadius: 4
                },
                {
                    label: 'Unattempted',
                    data: data.unattempted,
                    backgroundColor: 'rgba(108, 117, 125, 0.6)',
                    borderColor: '#6c757d',
                    borderWidth: 1.5,
                    borderRadius: 4
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                x: {
                    grid: { display: false },
                    ticks: { font: { size: 11, weight: '600' } }
                },
                y: {
                    beginAtZero: true,
                    max: 25,
                    ticks: {
                        stepSize: 5,
                        font: { size: 11 }
                    },
                    grid: { color: '#f0f0f0' }
                }
            },
            plugins: {
                legend: {
                    position: 'top',
                    labels: {
                        padding: 14,
                        font: { size: 12, weight: '600' },
                        usePointStyle: true
                    }
                },
                tooltip: {
                    callbacks: {
                        label: ctx => ` ${ctx.dataset.label}: ${ctx.parsed.y}`
                    }
                }
            }
        }
    });
}

// ============================================================
// REVIEW FILTER BUTTONS
// ============================================================
function initReviewFilters() {
    const filterBtns = document.querySelectorAll('[data-filter]');
    if (!filterBtns.length) return;

    filterBtns.forEach(btn => {
        btn.addEventListener('click', function () {
            filterBtns.forEach(b => b.classList.remove('active'));
            this.classList.add('active');
            const filter = this.dataset.filter;
            applyReviewFilter(filter);
        });
    });

    // Show all by default
    applyReviewFilter('all');
}

function applyReviewFilter(filter) {
    const items = document.querySelectorAll('.question-review-item');
    items.forEach(item => {
        const status = item.dataset.status;
        let show = false;
        if (filter === 'all') show = true;
        else if (filter === 'correct' && status === 'correct') show = true;
        else if (filter === 'incorrect' && status === 'incorrect') show = true;
        else if (filter === 'skipped' && status === 'skipped') show = true;
        item.classList.toggle('show', show);
    });

    // Update count badge
    const visible = document.querySelectorAll('.question-review-item.show').length;
    const countEl = document.getElementById('review-count');
    if (countEl) countEl.textContent = visible;
}

// ============================================================
// SCORE CIRCLE ANIMATION
// ============================================================
function animateScoreCircle() {
    const el = document.getElementById('animated-score');
    if (!el) return;
    const target = parseFloat(el.dataset.target) || 0;
    const isDecimal = el.dataset.decimal === 'true';
    let current = 0;
    const duration = 1200;
    const steps = 60;
    const increment = target / steps;
    const interval = duration / steps;

    const timer = setInterval(() => {
        current += increment;
        if (current >= target) {
            current = target;
            clearInterval(timer);
        }
        el.textContent = isDecimal ? current.toFixed(2) : Math.floor(current);
    }, interval);
}
