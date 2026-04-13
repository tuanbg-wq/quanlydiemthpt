(function () {
  function prefersReducedMotion() {
    return window.matchMedia && window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  }

  function parseIntSafe(value) {
    var parsed = parseInt(value || "0", 10);
    return Number.isFinite(parsed) ? Math.max(0, parsed) : 0;
  }

  function parseFloatSafe(value) {
    var parsed = parseFloat((value || "0").replace(",", "."));
    return Number.isFinite(parsed) ? Math.max(0, parsed) : 0;
  }

  function animate(drawFn) {
    if (prefersReducedMotion()) {
      drawFn(1);
      return;
    }
    var duration = 900;
    var start = performance.now();
    function tick(now) {
      var progress = Math.min((now - start) / duration, 1);
      var eased = 1 - Math.pow(1 - progress, 3);
      drawFn(eased);
      if (progress < 1) {
        requestAnimationFrame(tick);
      }
    }
    requestAnimationFrame(tick);
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
    return { context: context, width: width, height: height };
  }

  function prepareSquareCanvas(canvas) {
    var context = canvas.getContext("2d");
    var rect = canvas.getBoundingClientRect();
    var ratio = window.devicePixelRatio || 1;
    var size = Math.max(1, Math.min(rect.width, rect.height));
    canvas.width = Math.floor(size * ratio);
    canvas.height = Math.floor(size * ratio);
    context.setTransform(ratio, 0, 0, ratio, 0, 0);
    return { context: context, width: size, height: size };
  }

  function drawScoreDonut() {
    var container = document.querySelector(".score-donut");
    var canvas = document.getElementById("scoreChartCanvas");
    var goodPlusText = document.querySelector(".score-good-plus-value");
    if (!container || !canvas || !goodPlusText) {
      return;
    }

    var excellentRate = parseFloatSafe(container.dataset.excellentRate);
    var goodRate = parseFloatSafe(container.dataset.goodRate);
    var averageRate = parseFloatSafe(container.dataset.averageRate);
    var weakRate = parseFloatSafe(container.dataset.weakRate);
    var goodPlusRate = Math.max(0, Math.min(100, excellentRate + goodRate));

    function render(progress) {
      var prepared = prepareSquareCanvas(canvas);
      var ctx = prepared.context;
      var width = prepared.width;
      var height = prepared.height;
      ctx.clearRect(0, 0, width, height);

      var centerX = width / 2;
      var centerY = height / 2;
      var radius = Math.min(width, height) * 0.36;
      var lineWidth = Math.max(16, radius * 0.3);
      var startAngle = -Math.PI / 2;

      ctx.lineWidth = lineWidth;
      ctx.lineCap = "round";
      ctx.strokeStyle = "#e2e8f0";
      ctx.beginPath();
      ctx.arc(centerX, centerY, radius, 0, Math.PI * 2);
      ctx.stroke();

      var segments = [
        { value: excellentRate, color: "#2f66db" },
        { value: goodRate, color: "#18b38f" },
        { value: averageRate, color: "#f59e0b" },
        { value: weakRate, color: "#ef4444" }
      ];

      segments.forEach(function (segment) {
        if (segment.value <= 0) {
          return;
        }
        var angle = (segment.value / 100) * Math.PI * 2 * progress;
        ctx.strokeStyle = segment.color;
        ctx.beginPath();
        ctx.arc(centerX, centerY, radius, startAngle, startAngle + angle);
        ctx.stroke();
        startAngle += angle;
      });

      goodPlusText.textContent = (goodPlusRate * progress).toFixed(1).replace(".0", "") + "%";
    }

    animate(render);
    window.addEventListener("resize", function () {
      render(1);
    });
  }

  function drawGenderDonut() {
    var container = document.querySelector(".gender-chart");
    var canvas = document.getElementById("genderChartCanvas");
    if (!container || !canvas) {
      return;
    }

    var male = parseIntSafe(container.dataset.male);
    var female = parseIntSafe(container.dataset.female);
    var total = male + female;

    function render(progress) {
      var prepared = prepareSquareCanvas(canvas);
      var ctx = prepared.context;
      var width = prepared.width;
      var height = prepared.height;
      ctx.clearRect(0, 0, width, height);

      var centerX = width / 2;
      var centerY = height / 2;
      var radius = Math.min(width, height) * 0.33;
      var lineWidth = Math.max(16, radius * 0.3);
      var startAngle = -Math.PI / 2;

      ctx.lineWidth = lineWidth;
      ctx.lineCap = "round";
      ctx.strokeStyle = "#e2e8f0";
      ctx.beginPath();
      ctx.arc(centerX, centerY, radius, 0, Math.PI * 2);
      ctx.stroke();

      var values = [male, female];
      var colors = ["#2f66db", "#ec4899"];
      values.forEach(function (value, index) {
        if (total <= 0 || value <= 0) {
          return;
        }
        var angle = (value / total) * Math.PI * 2 * progress;
        ctx.strokeStyle = colors[index];
        ctx.beginPath();
        ctx.arc(centerX, centerY, radius, startAngle, startAngle + angle);
        ctx.stroke();
        startAngle += angle;
      });

      ctx.fillStyle = "#173a5e";
      ctx.font = '700 26px "Be Vietnam Pro"';
      ctx.textAlign = "center";
      ctx.textBaseline = "middle";
      ctx.fillText(String(Math.round(total * progress)), centerX, centerY - 2);

      ctx.fillStyle = "#6a7f95";
      ctx.font = '600 12px "Be Vietnam Pro"';
      ctx.fillText("Hoc sinh", centerX, centerY + 20);
    }

    animate(render);
    window.addEventListener("resize", function () {
      render(1);
    });
  }

  function drawConductChart() {
    var container = document.querySelector(".conduct-chart");
    var canvas = document.getElementById("conductChartCanvas");
    var buttons = Array.from(document.querySelectorAll(".chart-switch-btn"));
    if (!container || !canvas || !buttons.length) {
      return;
    }

    var rewardRate = parseFloatSafe(container.dataset.rewardRate);
    var disciplineRate = parseFloatSafe(container.dataset.disciplineRate);
    var totalRate = Math.max(0, rewardRate + disciplineRate);
    var activeType = "donut";

    function drawDonut(ctx, width, height, progress) {
      var centerX = width / 2;
      var centerY = height / 2;
      var radius = Math.min(width, height) * 0.32;
      var lineWidth = Math.max(16, radius * 0.28);
      var start = -Math.PI / 2;

      ctx.lineWidth = lineWidth;
      ctx.lineCap = "round";
      ctx.strokeStyle = "#dee6f2";
      ctx.beginPath();
      ctx.arc(centerX, centerY, radius, 0, Math.PI * 2);
      ctx.stroke();

      var rewardAngle = totalRate <= 0 ? 0 : (rewardRate / totalRate) * Math.PI * 2 * progress;
      var disciplineAngle = totalRate <= 0 ? 0 : (disciplineRate / totalRate) * Math.PI * 2 * progress;

      if (rewardAngle > 0) {
        ctx.strokeStyle = "#1cb5a4";
        ctx.beginPath();
        ctx.arc(centerX, centerY, radius, start, start + rewardAngle);
        ctx.stroke();
      }
      if (disciplineAngle > 0) {
        ctx.strokeStyle = "#ef4444";
        ctx.beginPath();
        ctx.arc(centerX, centerY, radius, start + rewardAngle, start + rewardAngle + disciplineAngle);
        ctx.stroke();
      }

      ctx.fillStyle = "#1b3655";
      ctx.font = '700 24px "Be Vietnam Pro"';
      ctx.textAlign = "center";
      ctx.textBaseline = "middle";
      ctx.fillText((rewardRate * progress).toFixed(1).replace(".0", "") + "%", centerX, centerY - 2);
      ctx.fillStyle = "#6a7f95";
      ctx.font = '600 12px "Be Vietnam Pro"';
      ctx.fillText("Khen thuong", centerX, centerY + 20);
    }

    function drawBar(ctx, width, height, progress) {
      var labels = ["Khen thuong", "Ky luat"];
      var values = [rewardRate, disciplineRate];
      var colors = ["#1cb5a4", "#ef4444"];
      var paddingX = 46;
      var top = 20;
      var bottom = height - 36;
      var chartHeight = bottom - top;
      var barWidth = Math.min(90, (width - paddingX * 2) / 3);
      var gap = barWidth * 0.7;
      var startX = (width - (barWidth * 2 + gap)) / 2;

      ctx.strokeStyle = "#dce6f2";
      ctx.beginPath();
      ctx.moveTo(paddingX, bottom);
      ctx.lineTo(width - paddingX, bottom);
      ctx.stroke();

      values.forEach(function (value, index) {
        var barHeight = (value / 100) * chartHeight * progress;
        var x = startX + index * (barWidth + gap);
        var y = bottom - barHeight;

        ctx.fillStyle = colors[index];
        ctx.fillRect(x, y, barWidth, barHeight);
        ctx.fillStyle = "#1b3655";
        ctx.font = '700 13px "Be Vietnam Pro"';
        ctx.textAlign = "center";
        ctx.fillText((value * progress).toFixed(1).replace(".0", "") + "%", x + barWidth / 2, y - 8);
        ctx.fillStyle = "#6a7f95";
        ctx.font = '600 12px "Be Vietnam Pro"';
        ctx.fillText(labels[index], x + barWidth / 2, bottom + 16);
      });
    }

    function drawLine(ctx, width, height, progress) {
      var points = [
        { label: "Khen thuong", value: rewardRate * progress, color: "#1cb5a4" },
        { label: "Ky luat", value: disciplineRate * progress, color: "#ef4444" }
      ];
      var left = 56;
      var right = width - 56;
      var top = 24;
      var bottom = height - 40;
      var chartHeight = bottom - top;

      ctx.strokeStyle = "#dce6f2";
      ctx.beginPath();
      ctx.moveTo(left, bottom);
      ctx.lineTo(right, bottom);
      ctx.stroke();

      var mapped = points.map(function (point, index) {
        var x = points.length === 1 ? (left + right) / 2 : left + index * (right - left) / (points.length - 1);
        var y = bottom - (point.value / 100) * chartHeight;
        return { x: x, y: y, label: point.label, value: point.value, color: point.color };
      });

      ctx.strokeStyle = "#2f66db";
      ctx.lineWidth = 3;
      ctx.beginPath();
      mapped.forEach(function (point, index) {
        if (index === 0) {
          ctx.moveTo(point.x, point.y);
        } else {
          ctx.lineTo(point.x, point.y);
        }
      });
      ctx.stroke();

      mapped.forEach(function (point) {
        ctx.fillStyle = point.color;
        ctx.beginPath();
        ctx.arc(point.x, point.y, 6, 0, Math.PI * 2);
        ctx.fill();

        ctx.fillStyle = "#1b3655";
        ctx.font = '700 13px "Be Vietnam Pro"';
        ctx.textAlign = "center";
        ctx.fillText(point.value.toFixed(1).replace(".0", "") + "%", point.x, point.y - 12);
        ctx.fillStyle = "#6a7f95";
        ctx.font = '600 12px "Be Vietnam Pro"';
        ctx.fillText(point.label, point.x, bottom + 18);
      });
    }

    function render(progress) {
      var prepared = prepareCanvas(canvas);
      var ctx = prepared.context;
      var width = prepared.width;
      var height = prepared.height;
      ctx.clearRect(0, 0, width, height);

      if (activeType === "bar") {
        drawBar(ctx, width, height, progress);
        return;
      }
      if (activeType === "line") {
        drawLine(ctx, width, height, progress);
        return;
      }
      drawDonut(ctx, width, height, progress);
    }

    buttons.forEach(function (button) {
      button.addEventListener("click", function () {
        var nextType = button.dataset.chartType;
        if (!nextType || nextType === activeType) {
          return;
        }
        activeType = nextType;
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

  drawScoreDonut();
  drawConductChart();
  drawGenderDonut();
})();
