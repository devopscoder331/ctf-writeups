import os

from app import app
from models import db, Song, User


def fill_db():
    song = Song.query.filter_by(
        title='Photograph', artist='Ed Sheeran').first()
    if not song:
        song = Song(
            title='Photograph',
            artist='Ed Sheeran',
            is_moderated=True,
            lyrics='''Lovin' can hurt
Lovin' can hurt sometimes
But it's the only thing that I know
And when it gets hard
You know it can get hard sometimes
It is the only thing that makes us feel alive

We keep this love in a photograph
We made these memories for ourselves
Where our eyes are never closin'
Hearts are never broken
And time's forever frozen still

So you can keep me
Inside the pocket of your ripped jeans
Holdin' me closer 'til our eyes meet
You won't ever be alone
Wait for me to come home

Lovin' can heal
Lovin' can mend your soul
And it's the only thing that I know, know
I swear it will get easier
Remember that with every piece of ya
Mm, and it's the only thing we take with us when we die

Mm, we keep this love in this photograph
We made these memories for ourselves
Where our eyes are never closin'
Hearts were never broken
And time's forever frozen still

So you can keep me
Inside the pocket of your ripped jeans
Holdin' me closer 'til our eyes meet
You won't ever be alone
And if you hurt me
Well, that's okay, baby, only words bleed
Inside these pages, you just hold me
And I won't ever let you go
Wait for me to come home

Wait for me to come home
Wait for me to come home
Wait for me to come home

Oh, you can fit me
Inside that necklace you got when you were sixteen
Next to your heartbeat, where I should be
Keep it deep within your soul
And if you hurt me
Well, that's okay, baby, only words bleed
Inside these pages, you just hold me
And I won't ever let you go
When I'm away, I will remember how you kissed me
Under the lamppost back on Sixth Street
Hearin' you whisper through the phone
"Wait for me to come home"'''
        )
        db.session.add(song)

    song = Song.query.filter_by(title='Hawaiian Roller Coaster Ride',
                                artist='Kamehameha Schools Children’s Chorus & Mark Keali’i Ho’omalu').first()
    if not song:
        song = Song(
            title='Hawaiian Roller Coaster Ride',
            artist='Kamehameha Schools Children’s Chorus & Mark Keali’i Ho’omalu',
            is_moderated=True,
            lyrics='''Aloha ē, aloha ē (Aloha ē, aloha ē)
'Ano'ai ke aloha ē ('Ano'ai ke aloha ē)
Aloha ē, aloha ē (Aloha ē, aloha ē)
'Ano'ai ke aloha ē ('Ano'ai ke aloha ē)

There's no place I'd rather be
Than on my surfboard out at sea
Lingering in the ocean blue
And if I had one wish come true
I'd surf 'til the sun sets beyond the horizon
'Āwikiwiki, mai lohilohi
Lawe mai i ko papa he'e nalu
Flying by on the Hawaiian roller coaster ride

'Āwikiwiki, mai lohilohi
Lawe mai i ko papa he'e nalu
Pi'i nā nalu lā lahalaha
'O ka moana hānupanupa
Lalala i ka lā hanahana
Me ke kai hoene i ka pu'e one
Hele, hele mai kākou ē
Hawaiian roller coaster ride
See upcoming pop shows

There's no place I'd rather be
Than on the seashore, dry, wet, free
On golden sand is where I lay
And if I only had my way
I'd play 'til the sun sets beyond the horizon
Lalala i kala hanahana
Me ke kai hoene i ka pu'e one
It's time to try the Hawaiian roller coaster ride

Hang loose, hang ten, howzit, shake a shaka
No worry, no fear, ain't no biggie, braddah
Cuttin' in, cuttin' up, cuttin' back, cuttin' out
Frontside, backside, goofy footed, wipe out
Let's get jumpin', surf's up and pumpin'
Coastin' with the motion of the ocean
Whirlpools swirling, cascading, twirling
Hawaiian roller coaster ride


There's no place I'd rather be
Than on my surfboard out at sea
Lingering in the ocean blue
And if I had one wish come true
I'd surf 'til the sun sets beyond the horizon
'Āwikiwiki, mai lohilohi
Lawe mai i ko papa he'e nalu
Flying by on the Hawaiian roller coaster ride

'Āwikiwiki, mai lohilohi
Lawe mai i ko papa he'e nalu
Pi'i nā nalu lā lahalaha
'O ka moana hānupanupa
Lalala i ka lā hanahana
Me ke kai hoene i ka pu'e one
Hele, hele mai kākou ē
Hawaiian roller coaster ride'''
        )
        db.session.add(song)

    song = Song.query.filter_by(
        title='Baby Shark', artist='Pinkfong (핑크퐁)').first()
    if not song:
        song = Song(
            title='Baby Shark',
            artist='Pinkfong (핑크퐁)',
            is_moderated=True,
            lyrics='''Baby shark, doo-doo-doo-doo-doo-doo
Baby shark, doo-doo-doo-doo-doo-doo
Baby shark, doo-doo-doo-doo-doo-doo
Baby shark

Mommy shark, doo-doo-doo-doo-doo-doo
Mommy shark, doo-doo-doo-doo-doo-doo
Mommy shark, doo-doo-doo-doo-doo-doo
Mommy shark

Daddy shark, doo-doo-doo-doo-doo-doo
Daddy shark, doo-doo-doo-doo-doo-doo
Daddy shark, doo-doo-doo-doo-doo-doo
Daddy shark

Grandma shark, doo-doo-doo-doo-doo-doo
Grandma shark, doo-doo-doo-doo-doo-doo
Grandma shark, doo-doo-doo-doo-doo-doo
Grandma shark

Grandpa shark, doo-doo-doo-doo-doo-doo
Grandpa shark, doo-doo-doo-doo-doo-doo
Grandpa shark, doo-doo-doo-doo-doo-doo
Grandpa shark

Let's go hunt, doo-doo-doo-doo-doo-doo
Let's go hunt, doo-doo-doo-doo-doo-doo
Let's go hunt, doo-doo-doo-doo-doo-doo
Let's go hunt

Run away, doo-doo-doo-doo-doo-doo
Run away, doo-doo-doo-doo-doo-doo
Run away, doo-doo-doo-doo-doo-doo
Run away

Safe at last, doo-doo-doo-doo-doo-doo
Safe at last, doo-doo-doo-doo-doo-doo
Safe at last, doo-doo-doo-doo-doo-doo
Safe at last

It's the end, doo-doo-doo-doo-doo-doo
It's the end, doo-doo-doo-doo-doo-doo
It's the end, doo-doo-doo-doo-doo-doo
It's the end'''
        )
        db.session.add(song)

    song = Song.query.filter_by(
        title='WILDFLOWER', artist='Billie Eilish').first()
    if not song:
        song = Song(
            title='WILDFLOWER',
            artist='Billie Eilish',
            is_moderated=True,
            lyrics='''Things fall apart
And time breaks your heart
I wasn't there, but I know
She was your girl
You showed her the world
But fell out of love and you both let go

She was cryin’ on my shoulder
All I could do was hold her
Only made us closer until July
Now, I know that you love me
You don't need to remind me
I should put it all behind me, shouldn't I?

But I see her in the back of my mind all the time
Like a fever, like I’m burning alive, like a sign
Did I cross the line?
(Mm) Hmm

Well, good things don't last (Good things don't last)
And life moves so fast (Life moves so fast)
I'd never ask who was better (I'd never ask who was better)
'Cause she couldn't be (Couldn't)
More different from me (Different)
Happy and free in leather (Happy)

And I know that you love me (You love me)
You don’t need to remind me (Remind me)
Wanna put it all behind me, but baby

I see her in the back of my mind (Back of my mind) all the time (All the time)
Feels like a fever (Like a fever), like I’m burning alive (Burning alive), like a sign
Did I cross the line?

You say no one knows you so well (Oh)
But every time you touch me, I just wonder how she felt
Valentine's Day, cryin’ in the hotel
I know you didn't mean to hurt me, so I kept it to myself

And I wonder
Do you see her in the back of your mind in my eyes?

You say no one knows you so well
But every time you touch me, I just wonder how she felt
Valentine's Day, cryin' in the hotel
I know you didn’t mean to hurt me, so I kept it to myself'''
        )
        db.session.add(song)

    song = Song.query.filter_by(title='Creep', artist='Radiohead').first()
    if not song:
        song = Song(
            title='Creep',
            artist='Radiohead',
            is_moderated=True,
            lyrics='''When you were here before, couldn't look you in the eye
You're just like an angel, your skin makes me cry
You float like a feather in a beautiful world
I wish I was special, you're so fuckin' special

But I'm a creep
I'm a weirdo
What the hell am I doin' here?
I don't belong here

I don't care if it hurts, I wanna have control
I want a perfect body, I want a perfect soul
I want you to notice when I'm not around
You're so fuckin' special, I wish I was special

But I'm a creep
I'm a weirdo
What the hell am I doin' here?
I don't belong here
Oh-oh, oh-oh

She's runnin' out the door
She's runnin' out
She run, run, run, run
Run
See upcoming rock shows
Get tickets for your favorite artists

Whatever makes you happy, whatever you want
You're so fuckin' special, I wish I was special
But I'm a creep
I'm a weirdo
What the hell am I doin' here?
I don't belong here
I don't belong here'''
        )
        db.session.add(song)

    song = Song.query.filter_by(
        title='Not Like Us', artist='Kendrick Lamar').first()
    if not song:
        song = Song(
            title='Not Like Us',
            artist='Kendrick Lamar',
            is_moderated=True,
            lyrics='''Psst, I see dead people
(Mustard on the beat, ho)

Ayy, Mustard on the beat, ho
Deebo any rap nigga, he a free throw
Man down, call an amberlamps, tell him, "Breathe, bro"
Nail a nigga to the cross, he walk around like Teezo
What's up with these jabroni-ass niggas tryna see Compton?
The industry can hate me, fuck 'em all and they mama
How many opps you really got? I mean, it's too many options
I'm finna pass on this body, I'm John Stockton
Beat your ass and hide the Bible if God watchin'
Sometimes you gotta pop out and show niggas
Certified boogeyman, I'm the one that up the score with 'em
Walk him down, whole time, I know he got some ho in him
Pole on him, extort shit, bully Death Row on him
Say, Drake, I hear you like 'em young
You better not ever go to cell block one
To any bitch that talk to him and they in love
Just make sure you hide your lil' sister from him
They tell me Chubbs the only one that get your hand-me-downs
And Party at the party playin' with his nose now
And Baka got a weird case, why is he around?
Certified Lover Boy? Certified pedophiles
Wop, wop, wop, wop, wop, Dot, fuck 'em up
Wop, wop, wop, wop, wop, I'ma do my stuff
Why you trollin' like a bitch? Ain't you tired?
Tryna strike a chord and it's probably A minor
See Kendrick Lamar Live
Get tickets as low as $100

They not like us, they not like us, they not like us
They not like us, they not like us, they not like us

You think the Bay gon' let you disrespect Pac, nigga?
I think that Oakland show gon' be your last stop, nigga
Did Cole foul, I don't know why you still pretendin'
What is the owl? Bird niggas and burnt bitches, go
The audience not dumb
Shape the stories how you want, hey, Drake, they're not slow
Rabbit hole is still deep, I can go further, I promise
Ain't that somethin'? B-Rad stands for bitch and you Malibu most wanted
Ain't no law, boy, you ball boy, fetch Gatorade or somethin'
Since 2009, I had this bitch jumpin'
You niggas'll get a wedgie, be flipped over your boxers
What OVO for? The "Other Vaginal Option"? Pussy
Nigga better straighten they posture, got famous all up in Compton
Might write this for the doctorate, tell the pop star quit hidin'
Fuck a caption, want action, no accident
And I'm hands-on, he fuck around, get polished
Fucked on Wayne girl while he was in jail, that's connivin'
Then get his face tatted like a bitch apologizin'
I'm glad DeRoz' came home, y'all didn't deserve him neither
From Alondra down to Central, nigga better not speak on Serena
And your homeboy need subpoena, that predator move in flocks
That name gotta be registered and placed on neighborhood watch
I lean on you niggas like another line of Wock'
Yeah, it's all eyes on me, and I'ma send it up to Pac, ayy
Put the wrong label on me, I'ma get 'em dropped, ayy
Sweet Chin Music and I won't pass the aux, ayy
How many stocks do I really have in stock? Ayy
One, two, three, four, five, plus five, ayy
Devil is a lie, he a 69 God, ayy
Freaky-ass niggas need to stay they ass inside, ayy
Roll they ass up like a fresh pack of 'za, ayy
City is back up, it's a must, we outside, ayy

They not like us, they not like us, they not like us
They not like us, they not like us, they not like us

Once upon a time, all of us was in chains
Homie still doubled down callin' us some slaves
Atlanta was the Mecca, buildin' railroads and trains
Bear with me for a second, let me put y'all on game
The settlers was usin' townfolk to make 'em richer
Fast-forward, 2024, you got the same agenda
You run to Atlanta when you need a check balance
Let me break it down for you, this the real nigga challenge
You called Future when you didn't see the club (Ayy, what?)
Lil Baby helped you get your lingo up (What?)
21 gave you false street cred
Thug made you feel like you a slime in your head (Ayy, what?)
Quavo said you can be from Northside (What?)
2 Chainz say you good, but he lied
You run to Atlanta when you need a few dollars
No, you not a colleague, you a fuckin' colonizer
The family matter and the truth of the matter
It was God's plan to show y'all the liar

Mm
Mm-mm
He a fan, he a fan, he a fan (Mm)
He a fan, he a fan, he a
Freaky-ass nigga, he a 69 God
Freaky-ass nigga, he a 69 God
Hey, hey, hey, hey, run for your life
Hey, hey, hey, hey, run for your life
Freaky-ass nigga, he a 69 God
Freaky-ass nigga, he a 69 God
Hey, hey, hey, hey, run for your life
Hey, hey, hey, hey, run for your life
Let me hear you say, "OV-ho" (OV-ho)
Say, "OV-ho" (OV-ho)
Then step this way, step that way
Then step this way, step that way

Are you my friend?
Are we locked in?
Then step this way, step that way
Then step this way, step that way'''
        )
        db.session.add(song)

    email = os.getenv('ADMIN_EMAIL', 'admin@example.com')
    user = User.query.filter_by(username='admin', email=email).first()
    if not user:
        user = User(username='admin', email=email)
        password = os.getenv('ADMIN_PASSWORD', 'admin')
        user.set_password(password)
        db.session.add(user)

    db.session.commit()


with app.app_context():
    fill_db()
