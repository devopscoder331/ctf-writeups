from flask import Flask, render_template, request, redirect, url_for, flash, jsonify, session
from flask_sqlalchemy import SQLAlchemy
from werkzeug.utils import secure_filename
from extensions import db
import os
import mammoth
import random
from datetime import datetime


app = Flask(__name__)
app.config['SECRET_KEY'] = 'gennady_is_the_best_seal_ever'
app.config['SQLALCHEMY_DATABASE_URI'] = os.environ.get('DATABASE_URL', 'sqlite:///gennady.db')
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
app.config['MAX_CONTENT_LENGTH'] = 16 * 1024 * 1024  


db.init_app(app)


from models import VisitCounter, Letter


with app.app_context():
    db.create_all()
    if not VisitCounter.query.first():
        initial_counter = VisitCounter(count=0)
        db.session.add(initial_counter)
        db.session.commit()


@app.route('/')
def index():
    counter = VisitCounter.query.first()
    counter.count += 1
    db.session.commit()
    ads = ['ad1.gif', 'ad2.gif', 'ad3.gif', 'ad4.gif']
    ad_index = int(request.args.get('ad', 0)) % len(ads)
    current_ad = ads[ad_index]
    next_ad = (ad_index + 1) % len(ads)
    return render_template('index.html', visit_count=counter.count, current_ad=current_ad, next_ad=next_ad)


@app.route('/submit', methods=['GET', 'POST'])
def submit_letter():
    if request.method == 'POST':
        author = request.form.get('author', '').strip()
        title = request.form.get('title', '').strip()
        file = request.files.get('letter')
        if not author or not title or not file:
            flash('Gennady needs ALL the information! (He is very particular.)', 'error')
            return redirect(url_for('submit_letter'))
        if file.filename == '':
            flash('Gennady needs a file to read! (Even though he cannot read.)', 'error')
            return redirect(url_for('submit_letter'))
        if not file.filename.lower().endswith('.docx'):
            flash('Gennady only accepts .docx files! (He is very picky about file formats.)', 'error')
            return redirect(url_for('submit_letter'))       
        try:
            result = mammoth.convert_to_html(file)
            html_content = result.value
            if len(html_content) > 4000:
                flash('The letter is too long! (Maximum 4000 characters after formatting)', 'error')
                return redirect(url_for('submit_letter'))
            new_letter = Letter(
                author=author,
                title=title,
                content=html_content,
                submitted_at=datetime.now()
            )
            db.session.add(new_letter)
            db.session.commit()
            flash('Gennady received your letter! (He can\'t read.)', 'success')
            return redirect(url_for('submit_letter'))
        except Exception as e:
            flash(f'Gennady is confused by your file! Error: {str(e)}', 'error')
            return redirect(url_for('submit_letter'))
    return render_template('submit.html')


@app.route('/archive')
def archive():
    page = request.args.get('page', 1, type=int)
    letters_per_page = 4
    total_letters = Letter.query.count()
    if total_letters == 0:
        letters = []
        has_more = False
    else:
        all_letters = Letter.query.all()
        random.shuffle(all_letters)
        start_idx = (page - 1) * letters_per_page
        end_idx = start_idx + letters_per_page
        letters = all_letters[start_idx:end_idx]
        has_more = end_idx < len(all_letters)
    return render_template('archive.html', letters=letters, has_more=has_more, current_page=page)


@app.route('/about')
def about():
    seals_endangered = True
    return render_template('about.html', seals_endangered=seals_endangered)


@app.route('/api/random-ad')
def random_ad():
    ads = [
        {'image': 'ad1.gif', 'text': 'GennadyCoin ICO! 100% SCAM!'},
        {'image': 'ad2.gif', 'text': 'Click here to feed Gennady (DO NOT CLICK)'},
        {'image': 'ad3.gif', 'text': 'FREE FISH! (Just kidding)'},
        {'image': 'ad4.gif', 'text': 'Gennady\'s Seal School - Learn to Blub!'}
    ]
    return jsonify(random.choice(ads))


if __name__ == '__main__':
    print("GENNA GET STARTED")
    app.run(debug=True, host='0.0.0.0', port=5000) 