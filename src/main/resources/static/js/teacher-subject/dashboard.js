(function () {
  function prefersReducedMotion() {
    return window.matchMedia && window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  }

  function animate(draw) {
    if (prefersReducedMotion()) {
      draw(1);
      return;
    }
    var duration = 850;
    var start = performance.now();
    function tick(now) {
      var progress = Math.min((now - start) / duration, 1);
      var eased = 1 - Math.pow(1 - progress, 3);
      draw(eased);
      if (progress < 1) {
        requestAnimationFrame(tick);
      }
    }
    requestAnimationFrame(tick);
  }

  function parseFloatSafe(value) {
    var parsed = parseFloat(String(value || "0").replace(",", "."));
    return Number.isFinite(parsed) ? parsed : 0;
  }

  function parseIntSafe(value) {
    var parsed = parseInt(value || "0", 10);
    return Number.isFinite(parsed) ? parsed : 0;
  }

  function prepareCanvas(canvas) {
    var context = canvas.getContext("2d");
    var rect = canvas.getBoundingClientRect();
    var ratio = window.devicePixelRatio || 1;
    var width = Math.max(1, rect.width);
    var height = Math.max(1, rect.height);
    canvas.width = Math.floor(width * ratio);
    canvas.height = Math.floor(height * ratio);
    context.setTransform(ratio, 0, 0, ratio, 0, 0);
    return { ctx: context, width: width, height: height };
  }

  function prepareSquareCanvas(canvas) {
    var prepared = prepareCanvas(canvas);
    var size = Math.min(prepared.width, prepared.height);
    return {
      ctx: prepared.ctx,
      width: size,
      height: size
    };
  }

  function drawEmptyState(ctx, width, height, message) {
    ctx.clearRect(0, 0, width, height);
    ctx.fillStyle = "#8aa0b7";
    ctx.font = '600 14px "Manrope"';
    ctx.textAlign = "center";
    ctx.textBaseline = "middle";
    ctx.fillText(message, width / 2, height / 2);
  }

  function readClassData() {
    return Array.prototype.slice.call(document.querySelectorAll(".class-data-item")).map(function (item) {
      return {
        label: item.dataset.label || item.dataset.className || "-",
        excellent: parseIntSafe(item.dataset.excellent),
        good: parseIntSafe(item.dataset.good),
        average: parseIntSafe(item.dataset.average),
        weak: parseIntSafe(item.dataset.weak),
        goodPlus: parseFloatSafe(item.dataset.goodPlus),
        averageScore: parseFloatSafe(item.dataset.averageScore)
      };
    });
  }

  function renderOverviewChart() {
    var canvas = document.getElementById("subjectOverviewChart");
    var buttons = Array.prototype.slice.call(document.querySelectorAll("[data-overview-chart]"));
    if (!canvas || !buttons.length) {
      return;
    }

    var dataset = readClassData();
    var activeMode = "bar";
    var colors = {
      excellent: "#2f66db",
      good: "#21a67f",
      average: "#f59e0b",
      weak: "#ef4444"
    };

    function drawAxes(ctx, width, height, left, top, right, bottom) {
      ctx.strokeStyle = "#d7e2ef";
      ctx.lineWidth = 1;
      ctx.beginPath();
      ctx.moveTo(left, top);
      ctx.lineTo(left, bottom);
      ctx.lineTo(right, bottom);
      ctx.stroke();

      for (var index = 0; index <= 4; index += 1) {
        var y = top + ((bottom - top) / 4) * index;
        ctx.strokeStyle = "rgba(215, 226, 239, .7)";
        ctx.beginPath();
        ctx.moveTo(left, y);
        ctx.lineTo(right, y);
        ctx.stroke();
      }
    }

    function drawBarChart(ctx, width, height, progress) {
      if (!dataset.length) {
        drawEmptyState(ctx, width, height, "Chua co du lieu lop de ve bieu do.");
        return;
      }

      var left = 56;
      var right = width - 28;
      var top = 24;
      var bottom = height - 48;
      var chartWidth = right - left;
      var chartHeight = bottom - top;
      var maxTotal = dataset.reduce(function (max, item) {
        return Math.max(max, item.excellent + item.good + item.average + item.weak);
      }, 0);
      var slotWidth = chartWidth / dataset.length;
      var barWidth = Math.min(70, slotWidth * 0.58);

      ctx.clearRect(0, 0, width, height);
      drawAxes(ctx, width, height, left, top, right, bottom);

      dataset.forEach(function (item, index) {
        var x = left + slotWidth * index + (slotWidth - barWidth) / 2;
        var y = bottom;
        [
          { key: "excellent", value: item.excellent },
          { key: "good", value: item.good },
          { key: "average", value: item.average },
          { key: "weak", value: item.weak }
        ].forEach(function (segment) {
          if (!segment.value || maxTotal <= 0) {
            return;
          }
          var barHeight = (segment.value / maxTotal) * chartHeight * progress;
          y -= barHeight;
          ctx.fillStyle = colors[segment.key];
          ctx.fillRect(x, y, barWidth, barHeight);
        });

        ctx.fillStyle = "#294561";
        ctx.font = '700 12px "Manrope"';
        ctx.textAlign = "center";
        ctx.fillText(item.label, x + barWidth / 2, bottom + 18);
      });
    }

    function drawLineChart(ctx, width, height, progress) {
      if (!dataset.length) {
        drawEmptyState(ctx, width, height, "Chua co du lieu lop de ve bieu do.");
        return;
      }

      var left = 52;
      var right = width - 24;
      var top = 24;
      var bottom = height - 42;
      var chartHeight = bottom - top;

      ctx.clearRect(0, 0, width, height);
      drawAxes(ctx, width, height, left, top, right, bottom);

      var points = dataset.map(function (item, index) {
        return {
          x: dataset.length === 1 ? (left + right) / 2 : left + index * (right - left) / (dataset.length - 1),
          y: bottom - (Math.max(0, Math.min(100, item.goodPlus)) / 100) * chartHeight * progress,
          label: item.label,
          value: item.goodPlus
        };
      });

      ctx.strokeStyle = "#2563eb";
      ctx.lineWidth = 3;
      ctx.beginPath();
      points.forEach(function (point, index) {
        if (index === 0) {
          ctx.moveTo(point.x, point.y);
        } else {
          ctx.lineTo(point.x, point.y);
        }
      });
      ctx.stroke();

      points.forEach(function (point) {
        ctx.fillStyle = "#0ea5e9";
        ctx.beginPath();
        ctx.arc(point.x, point.y, 6, 0, Math.PI * 2);
        ctx.fill();

        ctx.fillStyle = "#294561";
        ctx.font = '700 12px "Manrope"';
        ctx.textAlign = "center";
        ctx.fillText(Math.round(point.value * progress) + "%", point.x, point.y - 14);
        ctx.fillStyle = "#6a7f95";
        ctx.fillText(point.label, point.x, bottom + 18);
      });
    }

    function render(progress) {
      var prepared = prepareCanvas(canvas);
      var ctx = prepared.ctx;
      var width = prepared.width;
      var height = prepared.height;
      if (activeMode === "line") {
        drawLineChart(ctx, width, height, progress);
        return;
      }
      drawBarChart(ctx, width, height, progress);
    }

    buttons.forEach(function (button) {
      button.addEventListener("click", function () {
        var mode = button.dataset.overviewChart;
        if (!mode || mode === activeMode) {
          return;
        }
        activeMode = mode;
        buttons.forEach(function (item) {
          item.classList.toggle("active", item === button);
        });
        animate(render);
      });
    });

    animate(render);
    window.addEventListener("resize", function () {
      render(1);
    });
  }

  function renderDistributionChart() {
    var container = document.querySelector(".donut-shell");
    var canvas = document.getElementById("subjectDistributionChart");
    var centerValue = document.querySelector(".donut-center strong");
    if (!container || !canvas) {
      return;
    }

    var totalStudents = Math.max(0, parseIntSafe(container.dataset.totalStudents));
    var segments = [
      { value: parseFloatSafe(container.dataset.excellentRate), color: "#2f66db" },
      { value: parseFloatSafe(container.dataset.goodRate), color: "#21a67f" },
      { value: parseFloatSafe(container.dataset.averageRate), color: "#f59e0b" },
      { value: parseFloatSafe(container.dataset.weakRate), color: "#ef4444" }
    ];

    function render(progress) {
      var prepared = prepareSquareCanvas(canvas);
      var ctx = prepared.ctx;
      var size = prepared.width;
      var center = size / 2;
      var radius = size * 0.32;
      var lineWidth = Math.max(18, radius * 0.28);

      ctx.clearRect(0, 0, size, size);
      ctx.lineWidth = lineWidth;
      ctx.lineCap = "round";
      ctx.strokeStyle = "#e2ebf4";
      ctx.beginPath();
      ctx.arc(center, center, radius, 0, Math.PI * 2);
      ctx.stroke();

      var start = -Math.PI / 2;
      segments.forEach(function (segment) {
        if (segment.value <= 0) {
          return;
        }
        var angle = (segment.value / 100) * Math.PI * 2 * progress;
        ctx.strokeStyle = segment.color;
        ctx.beginPath();
        ctx.arc(center, center, radius, start, start + angle);
        ctx.stroke();
        start += angle;
      });

      if (centerValue) {
        centerValue.textContent = String(Math.round(totalStudents * progress));
      }
    }

    container.classList.add("is-updating");
    animate(render);
    window.setTimeout(function () {
      container.classList.remove("is-updating");
    }, prefersReducedMotion() ? 0 : 1000);
    window.addEventListener("resize", function () {
      render(1);
    });
  }

  function bindSpotlightTabs() {
    var tabs = Array.prototype.slice.call(document.querySelectorAll("[data-spotlight-tab]"));
    var tables = Array.prototype.slice.call(document.querySelectorAll("[data-spotlight-table]"));
    if (!tabs.length || !tables.length) {
      return;
    }

    tabs.forEach(function (tab) {
      tab.addEventListener("click", function () {
        var target = tab.dataset.spotlightTab;
        tabs.forEach(function (item) {
          item.classList.toggle("active", item === tab);
        });
        tables.forEach(function (table) {
          table.hidden = table.dataset.spotlightTable !== target;
        });
      });
    });
  }

  renderOverviewChart();
  renderDistributionChart();
  bindSpotlightTabs();
})();
