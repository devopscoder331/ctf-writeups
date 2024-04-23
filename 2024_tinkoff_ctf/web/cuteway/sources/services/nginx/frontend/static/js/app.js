$(window).on('load', function() {
    if (window.location.pathname === "/card") {
        var token = localStorage.getItem("token");
        var profile_id = localStorage.getItem("profile_id");

        if ( !token || !profile_id ) {
            window.location.replace('/login');
        } else {
            fetch(`/api/card/${profile_id}`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            })
            .then((results) => {
                if (results.status !== 200) {
                    localStorage.removeItem("token");
                    localStorage.removeItem("profile_id");
                    window.location.replace("/login");
                }
                return results.json();
            })
            .then((json) => {
                var descriptions = $(splitStringByLength(json["descriptions"], 45));
                $(".first-name").text(json["first_name"]);
                $(".second-name").text(json["second_name"]);
                $(".descriptions").html(descriptions);
            })
            .catch((err) => {
                if (err) {
                    console.log(err);
                    Swal.fire("Что-то пошло не так!", "Не удалось выполнить AJAX-запрос!", "error");
                } else {
                    Swal.fire.stopLoading();
                    Swal.fire.close();
                }
            });
        }
    }
    

    $('#register-btn').on('click', function (e) {
        fetch(`/api/register`, {
            method: 'POST',
            body: JSON.stringify({
                first_name: document.getElementById("first-name-input").value,
                second_name: document.getElementById("second-name-input").value,
                username: document.getElementById("username-input").value,
                password: document.getElementById("password-input").value,
                descriptions: document.getElementById("descriptions-input").value,
                g_recaptcha_response: grecaptcha.getResponse(),
            }),
            headers: {
                'Content-Type': 'application/json'
            }
        })
        .then((results) => {
            return results.json();
        })
        .then((json) => {
            if ("profile_id" in json) {
                window.location.replace('/login');
            } else if ("detail" in json) {
                $("#alert-block").empty();
                grecaptcha.reset()
                for (var detail of json['detail']) {
                    if (detail["type"] === "forbidden") {
                        $("#alert-block").append(
                            `<div class="alert alert-danger" role="alert">
                                <div class="iq-alert-text">${detail['msg']}</div>
                            </div>`
                        )
                    } else if (detail["input"] === "") {
                        $("#alert-block").append(
                            `<div class="alert alert-danger" role="alert">
                                <div class="iq-alert-text">Field '${detail['loc'][1]}' required</div>
                            </div>`
                        )
                    } else {
                        $("#alert-block").append(
                            `<div class="alert alert-danger" role="alert">
                                <div class="iq-alert-text">${detail['loc'][1]}: ${detail['msg']}</div>
                            </div>`
                        )
                    }
                }
            }
        })
        .catch((err) => {
            if (err) {
                Swal.fire("Что-то пошло не так!", "Не удалось выполнить AJAX-запрос!", "error");
            } else {
                Swal.fire.stopLoading();
                Swal.fire.close();
            }
        });
    });
    
    $('#login-btn').on('click', function (e) {
        fetch(`/api/login`, {
            method: 'POST',
            body: JSON.stringify({
                username: document.getElementById("username-input").value,
                password: document.getElementById("password-input").value
            }),
            headers: {
                'Content-Type': 'application/json'
            }
        })
        .then((results) => {
            return results.json();
        })
        .then((json) => {
            if ("access_token" in json) {
                localStorage.setItem('token', json['access_token']);
                localStorage.setItem('profile_id', json['profile_id']);
                window.location.replace(`/card`);
            } else if ("detail" in json) {
                $("#alert-block").empty();
                for (var detail of json['detail']) {
                    if (detail["type"] === "unauthorized") {
                        $("#alert-block").append(
                            `<div class="alert alert-danger" role="alert">
                                <div class="iq-alert-text">${detail['msg']}</div>
                            </div>`
                        )
                    } else if (detail["input"] === "") {
                        $("#alert-block").append(
                            `<div class="alert alert-danger" role="alert">
                                <div class="iq-alert-text">Field '${detail['loc'][1]}' required</div>
                            </div>`
                        )
                    } else {
                        $("#alert-block").append(
                            `<div class="alert alert-danger" role="alert">
                                <div class="iq-alert-text">${detail['loc'][1]}: ${detail['msg']}</div>
                            </div>`
                        )
                    }
                }
            }
        })
        .catch((err) => {
            if (err) {
                console.log(err)
                Swal.fire("Что-то пошло не так!", "Не удалось выполнить AJAX-запрос!", "error");
            } else {
                Swal.fire.stopLoading();
                Swal.fire.close();
            }
        });
    });

    $('#logout-btn').on('click', function (e) {
        localStorage.removeItem("token");
        localStorage.removeItem("profile_id");
        window.location.replace("/login");
    });

    function splitStringByLength(inputString, maxLength) {
        var result = [];
        for (var i = 0; i < inputString.length; i += maxLength) {
            result.push(inputString.substr(i, maxLength));
        }
        return `<p>${result.join('</p><p>')}</p>`;
    }
});