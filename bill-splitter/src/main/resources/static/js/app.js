// Auto-check the logged-in user's checkbox when present
document.addEventListener('DOMContentLoaded', function () {
    // Flash messages auto-dismiss after 5 seconds
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => {
            alert.style.transition = 'opacity .5s';
            alert.style.opacity = '0';
            setTimeout(() => alert.remove(), 500);
        }, 5000);
    });

    // Select all / none toggle for participant checkboxes
    const checkboxes = document.querySelectorAll('input[name="participantIds"]');
    if (checkboxes.length > 0) {
        const wrapper = checkboxes[0].closest('.checkbox-list');
        if (wrapper) {
            const controls = document.createElement('div');
            controls.style.cssText = 'display:flex;gap:.5rem;margin-bottom:.5rem;';
            controls.innerHTML = `
                <button type="button" class="btn btn-sm btn-outline" id="selectAll">Select All</button>
                <button type="button" class="btn btn-sm btn-outline" id="selectNone">Select None</button>
            `;
            wrapper.parentNode.insertBefore(controls, wrapper);

            document.getElementById('selectAll').addEventListener('click', () => {
                checkboxes.forEach(cb => cb.checked = true);
            });
            document.getElementById('selectNone').addEventListener('click', () => {
                checkboxes.forEach(cb => cb.checked = false);
            });
        }
    }

    // Amount input: format to 2dp on blur
    const amountInput = document.querySelector('input[name="amount"]');
    if (amountInput) {
        amountInput.addEventListener('blur', function () {
            if (this.value) {
                this.value = parseFloat(this.value).toFixed(2);
            }
        });
    }
});
