/* nav.js v2 — Smart sidebar navigation, resolves paths relative to current page */
(function() {
  const here = window.location.pathname;

  // Resolve a path relative to the current page's directory
  function resolve(path) {
    // path is always relative to form_flexfield/frontend as the "root"
    return path;
  }

  // Determine if a link is active by matching the filename
  function isActive(href) {
    const file = href.split('/').pop();
    return here.endsWith('/' + file) || here.endsWith(file) ? 'active' : '';
  }

  // Build absolute paths based on where we are
  // Detect current module by path segments
  const segments = here.split('/').filter(Boolean);
  let depth = 2; // default: two levels deep (module/frontend/page.html)
  if (here.includes('/integration/')) depth = 2;
  else if (here.includes('/workflow_taskflow/')) depth = 2;
  else if (here.includes('/eit_lookup/')) depth = 2;
  else if (here.includes('/module_report/')) depth = 2;
  else if (here.includes('/form_flexfield/')) depth = 2;

  const base = '../..'; // always two levels up to project root

  const links = [
    { section: 'Components' },
    { href: `${base}/form_flexfield/frontend/forms.html`,         label: 'Form Designer'      },
    { href: `${base}/form_flexfield/frontend/flexfields.html`,    label: 'Flexfield Manager'  },
    { href: `${base}/workflow_taskflow/frontend/workflow.html`,   label: 'Workflow Engine'    },
    { href: `${base}/workflow_taskflow/frontend/taskflow.html`,   label: 'Task Flow Builder'  },
    { href: `${base}/eit_lookup/frontend/eit.html`,               label: 'EIT Handler'        },
    { href: `${base}/eit_lookup/frontend/lookup.html`,            label: 'Lookup Customizer'  },
    { section: 'Reports' },
    { href: `${base}/module_report/frontend/module_customizer.html`, label: 'Module Customizer' },
    { href: `${base}/module_report/frontend/report_builder.html`,    label: 'Report Builder'    },
    { section: 'Integrations' },
    { href: `${base}/integration/frontend/employee_onboarding.html`,   label: 'Onboarding'        },
    { href: `${base}/integration/frontend/performance_management.html`, label: 'Performance Mgmt' },
    { href: `${base}/integration/frontend/customization.html`,          label: 'Dashboard'         },
  ];

  const target = document.getElementById('sidebar') || document.querySelector('.sidebar');
  if (!target) return;

  let html = '<div class="sidebar-logo"><h2>HRMS</h2><span>Customization Subsystem</span></div>';
  links.forEach(l => {
    if (l.section) {
      html += `<div class="sidebar-section">${l.section}</div>`;
    } else {
      html += `<a href="${l.href}" class="${isActive(l.href)}">${l.label}</a>`;
    }
  });

  target.innerHTML = html;
})();
