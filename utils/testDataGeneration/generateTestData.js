const postData = require('./axiosClient');
const generateMockData = require('./postTemplate');

async function main() {
    const args = process.argv.slice(2);
    if (args.length === 2 && args[0] === 'Post') {
        const token = args[1];
        const { posts } = generateMockData();

        for (const post of posts) {
            await postData(post, token);
        }
    } else {
        console.log('Usage: node generateTestData Post <Token>');
    }
}

main();
