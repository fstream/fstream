/**
 * fstream - ui
 * Copyright 2015 Webapplayers.com
 *
 */

function configState($stateProvider, $urlRouterProvider, $compileProvider) {

    // Optimize load start with remove binding information inside the DOM element
    $compileProvider.debugInfoEnabled(true);

    // Set default state
    $urlRouterProvider.otherwise("/common/login");
    $stateProvider

        // Landing page
        .state('landing', {
            url: "/landing_page",
            templateUrl: "views/landing_page.html",
            data: {
                pageTitle: 'Landing page',
                specialClass: 'landing-page'
            }
        })

        // Charts
        .state('charts', {
            url: "/charts",
            templateUrl: "views/charts.html",
            data: {
                pageTitle: 'Charts',
            }
        })

        // Streams
        .state('streams', {
            url: "/streams",
            templateUrl: "views/streams.html",
            data: {
                pageTitle: 'Streams',
            }
        })

        // History
        .state('history', {
            url: "/history",
            templateUrl: "views/history.html",
            data: {
                pageTitle: 'History',
            }
        })
    
        // Alerts
        .state('alerts', {
            url: "/alerts",
            templateUrl: "views/alerts.html",
            data: {
                pageTitle: 'Alerts',
            }
        })

        // Interface
        .state('interface', {
            abstract: true,
            url: "/interface",
            templateUrl: "views/common/content.html",
            data: {
                pageTitle: 'Interface'
            }
        })
        .state('interface.buttons', {
            url: "/buttons",
            templateUrl: "views/interface/buttons.html",
            data: {
                pageTitle: 'Colors and Buttons',
                pageDesc: 'The basic color palette'
            }
        })
        .state('interface.typography', {
            url: "/typography",
            templateUrl: "views/interface/typography.html",
            data: {
                pageTitle: 'Typography',
                pageDesc: 'The basic elements of typography'
            }
        })
        .state('interface.components', {
            url: "/components",
            templateUrl: "views/interface/components.html",
            data: {
                pageTitle: 'Components',
                pageDesc: 'Tabs, according, collapse and other UI components'
            }
        })
        .state('interface.icons', {
            url: "/icons",
            templateUrl: "views/interface/icons.html",
            data: {
                pageTitle: 'Icons',
                pageDesc: 'Two great icon libraries. Pe-icon-7-stroke and Font Awesome'
            }
        })
        .state('interface.panels', {
            url: "/panels",
            templateUrl: "views/interface/panels.html",
            data: {
                pageTitle: 'Panels',
                pageDesc: 'Two great icon libraries. Pe-icon-7-stroke and Font Awesome'
            }
        })
        .state('interface.alerts', {
            url: "/alerts",
            templateUrl: "views/interface/alerts.html",
            data: {
                pageTitle: 'Alerts',
                pageDesc: 'Notification and custom alerts'
            }
        })
        .state('interface.modals', {
            url: "/modals",
            templateUrl: "views/interface/modals.html",
            data: {
                pageTitle: 'Modals',
                pageDesc: 'Modal window examples'
            }
        })
        .state('interface.list', {
            url: "/list",
            templateUrl: "views/interface/list.html",
            data: {
                pageTitle: 'Nestable list',
                pageDesc: 'Nestable - Drag & drop hierarchical list.'
            }
        })

        .state('interface.tour', {
            url: "/tour",
            templateUrl: "views/interface/tour.html",
            data: {
                pageTitle: 'Tour',
                pageDesc: 'The easiest way to show people how to use your website.'
            }
        })

        // App views
        .state('app_views', {
            abstract: true,
            url: "/app_views",
            templateUrl: "views/common/content.html",
            data: {
                pageTitle: 'App Views'
            }
        })
        .state('app_views.timeline', {
            url: "/timeline",
            templateUrl: "views/app_views/timeline.html",
            data: {
                pageTitle: 'Timeline',
                pageDesc: 'Present your events in timeline style.'
            }
        })
        .state('app_views.contacts', {
            url: "/contacts",
            templateUrl: "views/app_views/contacts.html",
            data: {
                pageTitle: 'Contacts',
                pageDesc: 'Show users list in nice and color panels'
            }
        })
        .state('app_views.profile', {
            url: "/profile",
            templateUrl: "views/app_views/profile.html",
            data: {
                pageTitle: 'Profile',
                pageDesc: 'Show user data in clear profile design'
            }
        })
        .state('app_views.calendar', {
            url: "/calendar",
            templateUrl: "views/app_views/calendar.html",
            data: {
                pageTitle: 'Calendar',
                pageDesc: 'Full-sized, drag & drop event calendar.'
            }
        })
        .state('app_views.projects', {
            url: "/projects",
            templateUrl: "views/app_views/projects.html",
            data: {
                pageTitle: 'Projects',
                pageDesc: 'List of projects.'
            }
        })
        .state('app_views.projects_details', {
            url: "/projects_details",
            templateUrl: "views/app_views/projects_details.html",
            data: {
                pageTitle: 'Project',
                pageDesc: 'Project details page'
            }
        })
        .state('app_views.social_board', {
            url: "/social_board",
            templateUrl: "views/app_views/social_board.html",
            data: {
                pageTitle: 'Social board',
                pageDesc: 'Message board for social interactions.'
            }
        })
        .state('app_views.blog', {
            url: "/blog",
            templateUrl: "views/app_views/blog.html",
            data: {
                pageTitle: 'Blog',
                pageDesc: 'Article board for blog page.'
            }
        })
        .state('blog_details', {
            url: "/blog_details",
            templateUrl: "views/app_views/blog_details.html",
            data: {
                pageTitle: 'Article',
                pageDesc: 'Article blog page.'
            }
        })
        .state('app_views.forum', {
            url: "/forum",
            templateUrl: "views/app_views/forum.html",
            data: {
                pageTitle: 'Forum',
                pageDesc: 'Topics board for forum page.'
            }
        })
        .state('app_views.forum_details', {
            url: "/forum_details",
            templateUrl: "views/app_views/forum_details.html",
            data: {
                pageTitle: 'Topic',
                pageDesc: 'Topic for forum page.'
            }
        })

        // Transitions
        .state('transitions', {
            abstract: true,
            url: "/transitions",
            templateUrl: "views/common/content_blank.html",
            data: {
                pageTitle: 'Transitions'
            }
        })
        .state('transitions.overview', {
            url: "/overview",
            templateUrl: "views/transitions/overview.html",
            data: {
                pageTitle: 'Overview of transitions Effect',
            }
        })
        .state('transitions.transition_one', {
            url: "/transition_one",
            templateUrl: "views/transitions/transition_one.html",
        })
        .state('transitions.transition_two', {
            url: "/transition_two",
            templateUrl: "views/transitions/transition_two.html",
        })
        .state('transitions.transition_three', {
            url: "/transition_three",
            templateUrl: "views/transitions/transition_three.html",
        })
        .state('transitions.transition_four', {
            url: "/transition_four",
            templateUrl: "views/transitions/transition_four.html",
        })
        .state('transitions.transition_five', {
            url: "/transition_five",
            templateUrl: "views/transitions/transition_five.html",
        })

        // Charts
        .state('charts-bak', {
            abstract: true,
            url: "/charts",
            templateUrl: "views/common/content.html",
            data: {
                pageTitle: 'Charts'
            }
        })
        .state('charts.flot', {
            url: "/flot",
            templateUrl: "views/charts/flot.html",
            data: {
                pageTitle: 'Flot chart',
                pageDesc: 'Flot is a pure JavaScript plotting library for jQuery, with a focus on simple usage, attractive looks and interactive features.'
            }
        })
        .state('charts.chartjs', {
            url: "/chartjs",
            templateUrl: "views/charts/chartjs.html",
            data: {
                pageTitle: 'ChartJS',
                pageDesc: 'Simple HTML5 Charts using the canvas element'
            }
        })
        .state('charts.inline', {
            url: "/inline",
            templateUrl: "views/charts/inline.html",
            data: {
                pageTitle: 'Inline charts',
                pageDesc: 'Small inline charts directly in the browser using data supplied in the controller.'
            }
        })

        // Common views
        .state('common', {
            abstract: true,
            url: "/common",
            templateUrl: "views/common/content_empty.html",
            data: {
                pageTitle: 'Common'
            }
        })
        .state('common.login', {
            url: "/login",
            templateUrl: "views/common_app/login.html",
            data: {
                pageTitle: 'Login page',
                specialClass: 'blank'
            }
        })
        .state('common.register', {
            url: "/register",
            templateUrl: "views/common_app/register.html",
            data: {
                pageTitle: 'Register page',
                specialClass: 'blank'
            }
        })
        .state('common.error_one', {
            url: "/error_one",
            templateUrl: "views/common_app/error_one.html",
            data: {
                pageTitle: 'Error 404',
                specialClass: 'blank'
            }
        })
        .state('common.error_two', {
            url: "/error_two",
            templateUrl: "views/common_app/error_two.html",
            data: {
                pageTitle: 'Error 505',
                specialClass: 'blank'
            }
        })
        .state('common.lock', {
            url: "/lock",
            templateUrl: "views/common_app/lock.html",
            data: {
                pageTitle: 'Lock page',
                specialClass: 'blank'
            }
        })
        // Tables views
        .state('tables', {
            abstract: true,
            url: "/tables",
            templateUrl: "views/common/content.html",
            data: {
                pageTitle: 'Tables'
            }
        })
        .state('tables.tables_design', {
            url: "/tables_design",
            templateUrl: "views/tables/tables_design.html",
            data: {
                pageTitle: 'Tables design',
                pageDesc: 'Examples of various designs of tables.'
            }
        })
        .state('tables.ng_grid', {
            url: "/ng_grid",
            templateUrl: "views/tables/ng_grid.html",
            data: {
                pageTitle: 'ngGgrid',
                pageDesc: 'Examples of various designs of tables.'
            }
        })
        .state('tables.datatables', {
            url: "/datatables",
            templateUrl: "views/tables/datatables.html",
            data: {
                pageTitle: 'DataTables',
                pageDesc: 'Advanced interaction controls to any HTML table'
            }
        })

        // Forms views
        .state('forms', {
            abstract: true,
            url: "/forms",
            templateUrl: "views/common/content_small.html",
            data: {
                pageTitle: 'Forms'
            }
        })
        .state('forms.forms_elements', {
            url: "/forms_elements",
            templateUrl: "views/forms/forms_elements.html",
            data: {
                pageTitle: 'Forms elements',
                pageDesc: 'Examples of various form controls.'
            }
        })
        .state('forms.forms_extended', {
            url: "/forms_extended",
            templateUrl: "views/forms/forms_extended.html",
            data: {
                pageTitle: 'Forms extended',
                pageDesc: 'Examples of various extended form controls.'
            }
        })
        .state('forms.text_editor', {
            url: "/text_editor",
            templateUrl: "views/forms/text_editor.html",
            data: {
                pageTitle: 'Text editor',
                pageDesc: 'Examples of text editor.'
            }
        })
        .state('forms.wizard', {
            url: "/wizard",
            templateUrl: "views/forms/wizard.html",
            data: {
                pageTitle: 'Wizard',
                pageDesc: 'Build a form with wizard functionality.'
            }
        })

        // Grid system
        .state('grid_system', {
            url: "/grid_system",
            templateUrl: "views/grid_system.html",
            data: {
                pageTitle: 'Grid system',
            }
        })
}

angular
    .module('fstream')
    .config(configState)
    .run(function($rootScope, $state, editableOptions) {
        $rootScope.$state = $state;
        editableOptions.theme = 'bs3';
    });