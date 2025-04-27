const API_BASE_URL = '/api';

document.addEventListener('DOMContentLoaded', function() {
    const sidenavElems = document.querySelectorAll('.sidenav');
    M.Sidenav.init(sidenavElems);
    
    App.init();
});

Handlebars.registerHelper('formatDate', function(timestamp) {
    if (!timestamp) return '';
    const date = new Date(timestamp * 1000);
    return date.toLocaleString('ru-RU');
});

const App = {
    templates: {
        home: null,
        playCard: null,
        book: null,
        ticket: null,
        error: null
    },
    currentRoute: null,
    appContainer: null,
    
    init: function() {
        this.appContainer = document.getElementById('app');
        
        this.compileTemplates();
        
        this.setupRouter();
        
        this.navigateTo(window.location.hash || '#');
    },
    
    compileTemplates: function() {
        this.templates.home = Handlebars.compile(document.getElementById('home-template').innerHTML);
        this.templates.playCard = Handlebars.compile(document.getElementById('play-card-template').innerHTML);
        this.templates.book = Handlebars.compile(document.getElementById('book-template').innerHTML);
        this.templates.ticket = Handlebars.compile(document.getElementById('ticket-template').innerHTML);
        this.templates.error = Handlebars.compile(document.getElementById('error-template').innerHTML);
    },
    
    setupRouter: function() {
        window.addEventListener('hashchange', () => {
            this.navigateTo(window.location.hash);
        });
    },
    
    navigateTo: function(hash) {
        let route = hash.split('/');
        let page = route[0];
        
        if (page.startsWith('#')) {
            page = page.substring(1);
        }

        this.currentRoute = {
            page: page || 'home',
            params: route.slice(1)
        };
        
        this.renderPage();
    },
    
    renderPage: function() {
        switch (this.currentRoute.page) {
            case '':
            case 'home':
                this.renderHomePage();
                break;
            case 'book':
                this.renderBookPage(this.currentRoute.params[0]);
                break;
            case 't':
                this.renderTicketPage(this.currentRoute.params[0]);
                break;
            default:
                this.renderErrorPage('Страница не найдена');
        }
    },
    
    renderHomePage: function() {
        this.appContainer.innerHTML = this.templates.home();
        
        const playsListContainer = document.getElementById('plays-list');
        
        playsListContainer.innerHTML = '<div class="center"><div class="preloader-wrapper big active"><div class="spinner-layer spinner-teal-only"><div class="circle-clipper left"><div class="circle"></div></div><div class="gap-patch"><div class="circle"></div></div><div class="circle-clipper right"><div class="circle"></div></div></div></div></div>';
        
        axios.get(`${API_BASE_URL}/plays`)
            .then(response => {
                playsListContainer.innerHTML = '';
                
                response.data.forEach(play => {
                    const playHtml = this.templates.playCard(play);
                    playsListContainer.innerHTML += playHtml;
                });
            })
            .catch(error => {
                console.error('Error fetching plays:', error);
                playsListContainer.innerHTML = `<div class="col s12"><div class="card red lighten-5"><div class="card-content"><span class="card-title">Ошибка</span><p>Не удалось загрузить список спектаклей: ${error.message}</p></div></div></div>`;
            });
    },
    
    renderBookPage: function(playId) {
        if (!playId) {
            this.renderErrorPage('Не указан ID спектакля');
            return;
        }
        
        this.appContainer.innerHTML = '<div class="center"><div class="preloader-wrapper big active"><div class="spinner-layer spinner-teal-only"><div class="circle-clipper left"><div class="circle"></div></div><div class="gap-patch"><div class="circle"></div></div><div class="circle-clipper right"><div class="circle"></div></div></div></div></div>';
        
        axios.get(`${API_BASE_URL}/plays`)
            .then(response => {
                const play = response.data.find(p => p.id == playId);
                
                if (!play) {
                    this.renderErrorPage('Спектакль не найден');
                    return;
                }
                
                this.appContainer.innerHTML = this.templates.book(play);
                
                M.updateTextFields();
                
                const bookingForm = document.getElementById('booking-form');
                const ticketResult = document.getElementById('ticket-result');
                const ticketUrlLink = document.getElementById('ticket-url-link');
                const viewTicketLink = document.getElementById('view-ticket-link');
                
                bookingForm.addEventListener('submit', async (e) => {
                    e.preventDefault();
                    
                    const playId = document.getElementById('play-id').value;
                    const name = document.getElementById('name').value;
                    const age = document.getElementById('age').value;
                    const comment = document.getElementById('comment').value;
                    
                    try {
                        const response = await axios.post(`${API_BASE_URL}/book`, {
                            play_id: parseInt(playId),
                            name: name,
                            age: parseInt(age),
                            comment: comment || null
                        });
                        
                        const ticketUrl = response.data.view_url;
                        if (ticketUrlLink) {
                            ticketUrlLink.href = ticketUrl;
                            ticketUrlLink.textContent = ticketUrl;
                            ticketUrlLink.target = "_blank";
                        }
                        if (viewTicketLink) {
                            viewTicketLink.href = `#t/${response.data.token}`;
                        }
                        
                        ticketResult.style.display = 'block';
                        
                        ticketResult.scrollIntoView({ behavior: 'smooth' });
                    } catch (error) {
                        console.error('Error:', error);
                        
                        let errorMessage = 'Ошибка бронирования билета';
                        if (error.response && error.response.data && error.response.data.detail) {
                            errorMessage = error.response.data.detail;
                        } else if (error.message) {
                            errorMessage = error.message;
                        }
                        
                        M.toast({html: errorMessage, classes: 'red'});
                    }
                });
            })
            .catch(error => {
                console.error('Error fetching play:', error);
                this.renderErrorPage(`Не удалось загрузить информацию о спектакле: ${error.message}`);
            });
    },
    
    renderTicketPage: function(token) {
        if (!token) {
            this.renderErrorPage('Не указан токен билета');
            return;
        }
        
        this.appContainer.innerHTML = '<div class="center"><div class="preloader-wrapper big active"><div class="spinner-layer spinner-teal-only"><div class="circle-clipper left"><div class="circle"></div></div><div class="gap-patch"><div class="circle"></div></div><div class="circle-clipper right"><div class="circle"></div></div></div></div></div>';
        
        axios.get(`${API_BASE_URL}/ticket/${token}`)
            .then(response => {
                const data = {
                    ticket: response.data.ticket,
                    play: response.data.play,
                    token: token,
                    ticketUrl: `${window.location.origin}/#t/${token}`,
                    secret_word: response.data.secret_word,
                    eligible: response.data.eligible
                };
                
                this.appContainer.innerHTML = this.templates.ticket(data);
                
                const copyUrlBtn = document.getElementById('copy-url-btn');
                
                if (copyUrlBtn) {
                    copyUrlBtn.addEventListener('click', function() {
                        try {
                            const tempInput = document.createElement('input');
                            tempInput.value = `${window.location.origin}/#t/${token}`;
                            document.body.appendChild(tempInput);
                            tempInput.select();
                            document.execCommand('copy');
                            document.body.removeChild(tempInput);
                            
                            M.toast({html: 'URL скопирован в буфер обмена!', classes: 'teal'});
                        } catch (error) {
                            console.error('Error copying URL:', error);
                            M.toast({html: 'Не удалось скопировать URL', classes: 'red'});
                        }
                    });
                }
            })
            .catch(error => {
                console.error('Error fetching ticket:', error);
                
                let errorMessage = 'Не удалось загрузить информацию о билете';
                if (error.response && error.response.data && error.response.data.detail) {
                    errorMessage = error.response.data.detail;
                } else if (error.message) {
                    errorMessage = error.message;
                }
                
                this.renderErrorPage(errorMessage);
            });
    },
    
    renderErrorPage: function(error) {
        this.appContainer.innerHTML = this.templates.error({
            error: error
        });
    }
}; 