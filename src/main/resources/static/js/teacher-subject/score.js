(function () {
    function closeAllMenus() {
        document.querySelectorAll('.action-dropdown').forEach(function (menu) {
            menu.classList.remove('show');
            menu.classList.remove('open-up');
        });
        document.querySelectorAll('.action-menu.is-open').forEach(function (menu) {
            menu.classList.remove('is-open');
        });
        document.querySelectorAll('.table tbody tr.menu-open').forEach(function (row) {
            row.classList.remove('menu-open');
        });
        document.querySelectorAll('.action-toggle[aria-expanded="true"]').forEach(function (button) {
            button.setAttribute('aria-expanded', 'false');
        });
    }

    function positionMenu(button, menu) {
        const spacing = 8;
        menu.classList.add('show');
        menu.classList.remove('open-up');
        menu.style.left = '0px';
        menu.style.top = '0px';

        const buttonRect = button.getBoundingClientRect();
        const menuRect = menu.getBoundingClientRect();

        let left = buttonRect.right - menuRect.width;
        if (left < spacing) {
            left = spacing;
        }
        if (left + menuRect.width > window.innerWidth - spacing) {
            left = window.innerWidth - menuRect.width - spacing;
        }

        let top = buttonRect.bottom + spacing;
        const canOpenUp = buttonRect.top - menuRect.height - spacing >= spacing;
        const willOverflowDown = top + menuRect.height > window.innerHeight - spacing;

        if (willOverflowDown && canOpenUp) {
            top = buttonRect.top - menuRect.height - spacing;
            menu.classList.add('open-up');
        } else if (willOverflowDown) {
            top = Math.max(spacing, window.innerHeight - menuRect.height - spacing);
        }

        menu.style.left = left + 'px';
        menu.style.top = top + 'px';
    }

    window.toggleTeacherSubjectActionMenu = function (button) {
        const currentMenu = button.nextElementSibling;
        const currentRow = button.closest('tr');
        const shouldShow = !currentMenu.classList.contains('show');

        closeAllMenus();
        if (!shouldShow) {
            return;
        }

        positionMenu(button, currentMenu);
        button.setAttribute('aria-expanded', 'true');
        if (currentRow) {
            currentRow.classList.add('menu-open');
        }
        const currentWrap = button.closest('.action-menu');
        if (currentWrap) {
            currentWrap.classList.add('is-open');
        }
    };

    document.addEventListener('click', function (event) {
        if (!event.target.closest('.action-menu')) {
            closeAllMenus();
        }
    });
    window.addEventListener('resize', closeAllMenus);
    document.addEventListener('scroll', closeAllMenus, true);

    const deleteModal = document.getElementById('teacherSubjectScoreDeleteModal');
    const deleteModalMessage = document.getElementById('teacherSubjectScoreDeleteModalMessage');
    const cancelDeleteButton = document.getElementById('cancelTeacherSubjectScoreDeleteButton');
    const confirmDeleteButton = document.getElementById('confirmTeacherSubjectScoreDeleteButton');
    let pendingDeleteForm = null;

    function openDeleteModal(studentName, subjectName) {
        const who = studentName ? ' của học sinh "' + studentName + '"' : '';
        const subject = subjectName ? ' môn "' + subjectName + '"' : '';
        deleteModalMessage.textContent = 'Bạn có chắc chắn muốn xóa nhóm điểm' + who + subject + ' không?';
        deleteModal.hidden = false;
        document.body.classList.add('modal-open');
        confirmDeleteButton.focus();
        closeAllMenus();
    }

    function closeDeleteModal() {
        deleteModal.hidden = true;
        document.body.classList.remove('modal-open');
        pendingDeleteForm = null;
    }

    document.querySelectorAll('.score-delete-form').forEach(function (form) {
        form.addEventListener('submit', function (event) {
            if (form.dataset.confirmed === 'true') {
                form.dataset.confirmed = 'false';
                return;
            }
            event.preventDefault();
            pendingDeleteForm = form;
            openDeleteModal(form.dataset.studentName || '', form.dataset.subjectName || '');
        });
    });

    if (confirmDeleteButton) {
        confirmDeleteButton.addEventListener('click', function () {
            if (!pendingDeleteForm) {
                closeDeleteModal();
                return;
            }
            pendingDeleteForm.dataset.confirmed = 'true';
            pendingDeleteForm.submit();
        });
    }
    if (cancelDeleteButton) {
        cancelDeleteButton.addEventListener('click', closeDeleteModal);
    }
    if (deleteModal) {
        deleteModal.querySelectorAll('[data-close-teacher-subject-score-delete-modal]').forEach(function (button) {
            button.addEventListener('click', closeDeleteModal);
        });
    }

    function normalizeText(value) {
        return (value || '')
            .toLowerCase()
            .normalize('NFD')
            .replace(/[\u0300-\u036f]/g, '')
            .replace(/đ/g, 'd')
            .replace(/[^a-z0-9/\s:-]/g, ' ')
            .replace(/\s+/g, ' ')
            .trim();
    }

    function parseDisplayDateParts(value) {
        const match = (value || '').match(/(\d{2})\/(\d{2})\/(\d{4})/);
        if (!match) {
            return null;
        }
        return {
            day: match[1],
            month: match[2],
            year: match[3],
            iso: match[3] + '-' + match[2] + '-' + match[1],
            monthKey: match[3] + '-' + match[2]
        };
    }

    function createPaginationState(itemsPerPage) {
        return {
            page: 1,
            pageSize: itemsPerPage
        };
    }

    function renderPagination(container, totalItems, state, onChange) {
        if (!container) {
            return;
        }
        const totalPages = Math.max(1, Math.ceil(totalItems / state.pageSize));
        state.page = Math.min(Math.max(1, state.page), totalPages);

        container.innerHTML = '';
        container.hidden = totalItems <= state.pageSize;
        if (container.hidden) {
            return;
        }

        function createButton(label, page, disabled, active) {
            const button = document.createElement('button');
            button.type = 'button';
            button.className = 'page-btn' + (active ? ' active' : '') + (disabled ? ' disabled' : '');
            button.textContent = label;
            button.disabled = !!disabled;
            if (!disabled) {
                button.addEventListener('click', function () {
                    state.page = page;
                    onChange();
                });
            }
            container.appendChild(button);
        }

        createButton('‹', state.page - 1, state.page <= 1, false);
        for (let page = 1; page <= totalPages; page++) {
            createButton(String(page), page, false, page === state.page);
        }
        createButton('›', state.page + 1, state.page >= totalPages, false);
    }

    function bindPagedFilter(config) {
        const state = createPaginationState(4);

        function apply() {
            const filteredItems = config.items.filter(function (item) {
                return config.matches(item);
            });

            const totalPages = Math.max(1, Math.ceil(filteredItems.length / state.pageSize));
            state.page = Math.min(state.page, totalPages);
            const start = (state.page - 1) * state.pageSize;
            const end = start + state.pageSize;
            const visibleItems = filteredItems.slice(start, end);

            config.items.forEach(function (item) {
                item.hidden = visibleItems.indexOf(item) === -1;
            });

            if (config.emptyHint) {
                config.emptyHint.hidden = filteredItems.length > 0;
            }

            renderPagination(config.pagination, filteredItems.length, state, apply);
        }

        config.triggers.forEach(function (trigger) {
            if (!trigger) {
                return;
            }
            const eventName = trigger.tagName === 'BUTTON' ? 'click' : 'input';
            trigger.addEventListener(eventName, function () {
                state.page = 1;
                apply();
            });
            if (trigger.tagName === 'INPUT' && (trigger.type === 'date' || trigger.type === 'month' || trigger.type === 'number')) {
                trigger.addEventListener('change', function () {
                    state.page = 1;
                    apply();
                });
            }
        });

        apply();
    }

    const activitySearchInput = document.getElementById('activitySearchInput');
    const activityDateFilter = document.getElementById('activityDateFilter');
    const activityMonthFilter = document.getElementById('activityMonthFilter');
    const activityYearFilter = document.getElementById('activityYearFilter');
    const activityApplyFilterButton = document.getElementById('activityApplyFilterButton');
    const activityItems = Array.from(document.querySelectorAll('#scoreActivityList .activity-item'));
    const activityEmptyHint = document.getElementById('activityEmptyHint');
    const activityPagination = document.getElementById('activityPagination');

    if (activityItems.length) {
        bindPagedFilter({
            items: activityItems,
            emptyHint: activityEmptyHint,
            pagination: activityPagination,
            triggers: [
                activitySearchInput,
                activityDateFilter,
                activityMonthFilter,
                activityYearFilter,
                activityApplyFilterButton
            ],
            matches: function (item) {
                const keyword = normalizeText(activitySearchInput ? activitySearchInput.value : '');
                const yearValue = (activityYearFilter ? activityYearFilter.value : '').trim();
                const dateParts = parseDisplayDateParts(item.dataset.actionTime || '');
                const actorName = normalizeText(item.dataset.actorName);
                const actorRole = normalizeText(item.dataset.actorRole);
                const actionLabel = normalizeText(item.dataset.actionLabel);
                const actionDetail = normalizeText(item.dataset.actionDetail);
                const actionTime = normalizeText(item.dataset.actionTime);

                if (keyword && !(actorName.includes(keyword)
                    || actorRole.includes(keyword)
                    || actionLabel.includes(keyword)
                    || actionDetail.includes(keyword)
                    || actionTime.includes(keyword))) {
                    return false;
                }
                if (activityDateFilter && activityDateFilter.value && (!dateParts || dateParts.iso !== activityDateFilter.value)) {
                    return false;
                }
                if (activityMonthFilter && activityMonthFilter.value && (!dateParts || dateParts.monthKey !== activityMonthFilter.value)) {
                    return false;
                }
                if (yearValue && (!dateParts || dateParts.year !== yearValue)) {
                    return false;
                }
                return true;
            }
        });
    }

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape') {
            if (deleteModal && !deleteModal.hidden) {
                closeDeleteModal();
                return;
            }
            closeAllMenus();
        }
    });
})();
